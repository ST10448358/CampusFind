package com.segotlo.campusfindapp.ui.xml

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.Report
import com.segotlo.campusfindapp.data.ReportStatus
import com.segotlo.campusfindapp.data.ReportType
import com.segotlo.campusfindapp.databinding.FragmentAdminMatchesBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminMatchesFragment : Fragment() {
    private var _binding: FragmentAdminMatchesBinding? = null
    private val binding get() = _binding!!
    private lateinit var matchAdapter: AdminMatchAdapter

    private var selectedMatchFilter = 0 // 0: Pending, 1: Confirmed, 2: Dismissed, 3: All

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminMatchesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val handoverLocations = listOf("Campus Main Gate Desk", "Library Security Desk", "Student Union Office", "Science Building Security")
        val dropdownAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_white_text, handoverLocations)
        binding.actvHandoverLocation.setAdapter(dropdownAdapter)

        matchAdapter = AdminMatchAdapter(
            onConfirm = { lost, found ->
                lifecycleScope.launch {
                    MockRepository.updateReportStatus(lost.id, ReportStatus.CLAIMED, "Confirmed at Handover")
                    MockRepository.updateReportStatus(found.id, ReportStatus.CLAIMED, "Confirmed at Handover")
                    val pin = lost.handoverPin
                    val location = binding.actvHandoverLocation.text.toString().ifEmpty { "Campus Main Gate Desk" }
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Match Confirmed & PIN Generated")
                        .setMessage("Secure Handover PIN: $pin\nAssigned Desk: $location\n\nBoth students have been notified with this verification code.")
                        .setPositiveButton("OK") { d, _ -> d.dismiss() }
                        .show()
                }
            },
            onReject = { lost, found ->
                lifecycleScope.launch {
                    MockRepository.updateReportStatus(lost.id, ReportStatus.OPEN, null)
                    MockRepository.updateReportStatus(found.id, ReportStatus.OPEN, null)
                    Toast.makeText(requireContext(), "Match dismissed.", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.rvAdminMatches.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminMatches.adapter = matchAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                MockRepository.reports.collectLatest { reports ->
                    if (_binding != null) {
                        updateMatchesUI(reports)
                    }
                }
            }
        }

        binding.btnManualLinkReports.setOnClickListener {
            showManualLinkDialog()
        }

        binding.chipMatchPending.setOnClickListener { selectMatchFilter(0) }
        binding.chipMatchConfirmed.setOnClickListener { selectMatchFilter(1) }
        binding.chipMatchDismissed.setOnClickListener { selectMatchFilter(2) }
        binding.chipMatchAll.setOnClickListener { selectMatchFilter(3) }
    }

    private fun showManualLinkDialog() {
        val dialogBinding = com.segotlo.campusfindapp.databinding.DialogManualLinkBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        val allReports = MockRepository.reports.value
        val openReports = allReports.filter { it.status == ReportStatus.OPEN || it.status == ReportStatus.MATCH_SUGGESTED }

        if (openReports.isEmpty()) {
            dialogBinding.rvManualLinkReports.visibility = View.GONE
            dialogBinding.llManualLinkEmpty.visibility = View.VISIBLE
        } else {
            dialogBinding.rvManualLinkReports.visibility = View.VISIBLE
            dialogBinding.llManualLinkEmpty.visibility = View.GONE
        }

        val adapter = ManualLinkAdapter(
            onSelectionChanged = { selectedList ->
                val count = selectedList.size
                dialogBinding.btnConfirmManualLink.text = "Link Selected Reports ($count/2)"
                dialogBinding.btnConfirmManualLink.isEnabled = count == 2
            },
            onViewDetails = { report ->
                showReportDetailDialog(report)
            }
        )

        dialogBinding.rvManualLinkReports.layoutManager = LinearLayoutManager(requireContext())
        dialogBinding.rvManualLinkReports.adapter = adapter
        adapter.submitList(openReports)

        dialogBinding.btnConfirmManualLink.isEnabled = false

        dialogBinding.etDialogSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim().orEmpty()
                val filtered = if (query.isEmpty()) {
                    openReports
                } else {
                    openReports.filter {
                        it.title.contains(query, ignoreCase = true) ||
                        it.referenceCode.contains(query, ignoreCase = true) ||
                        it.userDisplayName.contains(query, ignoreCase = true)
                    }
                }
                adapter.submitList(filtered)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        dialogBinding.btnCloseDialog.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnCancelManualLink.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnConfirmManualLink.setOnClickListener {
            val selected = adapter.getSelectedReports()
            if (selected.size != 2) {
                Toast.makeText(requireContext(), "Please select exactly 2 reports to link", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val lostReport = selected.find { it.type == ReportType.LOST }
            val foundReport = selected.find { it.type == ReportType.FOUND }

            if (lostReport == null || foundReport == null) {
                Toast.makeText(
                    requireContext(),
                    "Please select 1 LOST report and 1 FOUND report to link.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val res = MockRepository.linkReports(lostReport.id, foundReport.id)
                if (res.isSuccess) {
                    Toast.makeText(
                        requireContext(),
                        "Reports '${lostReport.title}' & '${foundReport.title}' successfully linked!",
                        Toast.LENGTH_LONG
                    ).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Failed to link reports", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun showReportDetailDialog(report: Report) {
        val dialogBinding = com.segotlo.campusfindapp.databinding.DialogReportDetailBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvDetailTitle.text = report.title
        dialogBinding.tvDetailDesc.text = report.description
        dialogBinding.tvDetailLocation.text = "📍 ${report.institution} • ${report.location.name}"
        dialogBinding.tvDetailContact.text = "Reporter: ${report.userDisplayName} (${report.userEmail})"

        dialogBinding.btnDetailClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDetailClaim.text = "Close Preview"
        dialogBinding.btnDetailClaim.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun selectMatchFilter(index: Int) {
        selectedMatchFilter = index
        val chips = listOf(binding.chipMatchPending, binding.chipMatchConfirmed, binding.chipMatchDismissed, binding.chipMatchAll)
        chips.forEachIndexed { i, btn ->
            if (i == index) {
                btn.setBackgroundColor(Color.parseColor("#6D28D9"))
                btn.setTextColor(Color.WHITE)
            } else {
                btn.setBackgroundColor(Color.parseColor("#101726"))
                btn.setTextColor(Color.parseColor("#9CA3AF"))
            }
        }
        updateMatchesUI(MockRepository.reports.value)
    }

    private fun updateMatchesUI(reports: List<Report>) {
        if (_binding == null) return
        val pendingList = mutableListOf<Pair<Report, Report>>()
        val confirmedList = mutableListOf<Pair<Report, Report>>()
        val dismissedList = mutableListOf<Pair<Report, Report>>()

        val processedIds = mutableSetOf<String>()

        for (report in reports) {
            if (report.matchedWithId != null && !processedIds.contains(report.id)) {
                val partner = reports.find { it.id == report.matchedWithId }
                if (partner != null) {
                    processedIds.add(report.id)
                    processedIds.add(partner.id)

                    val pair = if (report.type == ReportType.LOST) {
                        Pair(report, partner)
                    } else {
                        Pair(partner, report)
                    }

                    if (report.status == ReportStatus.CLAIMED || report.status == ReportStatus.RETURNED) {
                        confirmedList.add(pair)
                    } else if (report.status == ReportStatus.MATCH_SUGGESTED) {
                        pendingList.add(pair)
                    } else {
                        dismissedList.add(pair)
                    }
                }
            }
        }

        binding.chipMatchPending.text = "Pending Admin Review ${pendingList.size}"
        binding.chipMatchConfirmed.text = "Confirmed Matches ${confirmedList.size}"
        binding.chipMatchDismissed.text = "Dismissed / Rejected ${dismissedList.size}"
        binding.chipMatchAll.text = "All Matches ${pendingList.size + confirmedList.size + dismissedList.size}"

        val activeList = when (selectedMatchFilter) {
            0 -> pendingList
            1 -> confirmedList
            2 -> dismissedList
            else -> pendingList + confirmedList + dismissedList
        }

        if (activeList.isEmpty()) {
            binding.llAdminMatchesEmpty.visibility = View.VISIBLE
            binding.rvAdminMatches.visibility = View.GONE
        } else {
            binding.llAdminMatchesEmpty.visibility = View.GONE
            binding.rvAdminMatches.visibility = View.VISIBLE
            matchAdapter.submitList(activeList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
