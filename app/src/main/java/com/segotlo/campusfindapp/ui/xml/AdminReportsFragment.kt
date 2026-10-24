package com.segotlo.campusfindapp.ui.xml

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.Report
import com.segotlo.campusfindapp.data.ReportStatus
import com.segotlo.campusfindapp.data.ReportType
import com.segotlo.campusfindapp.databinding.FragmentAdminReportsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminReportsFragment : Fragment() {
    private var _binding: FragmentAdminReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ReportAdapter

    private var selectedFilter = 0 // 0: All, 1: Pending, 2: Approved Lost, 3: Approved Found, 4: Returned

    private val speechResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrEmpty()) {
                binding.etAdminSearch.setText(spokenText)
                binding.etAdminSearch.setSelection(spokenText.length)
                Toast.makeText(requireContext(), "Voice search: \"$spokenText\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReportAdapter { report ->
            showManualMatchDialog(report)
        }
        binding.rvAdminReports.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminReports.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                MockRepository.reports.collectLatest { reports ->
                    if (_binding != null) {
                        updateChipsAndList(reports)
                    }
                }
            }
        }

        binding.etAdminSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateChipsAndList(MockRepository.reports.value)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.tilAdminSearch.setEndIconOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search admin reports...")
            }
            try {
                speechResultLauncher.launch(intent)
            } catch (_: Throwable) {
                Toast.makeText(requireContext(), "Speech recognition not available on this device", Toast.LENGTH_SHORT).show()
            }
        }

        binding.chipAll.setOnClickListener { selectFilter(0) }
        binding.chipPending.setOnClickListener { selectFilter(1) }
        binding.chipApprovedLost.setOnClickListener { selectFilter(2) }
        binding.chipApprovedFound.setOnClickListener { selectFilter(3) }
        binding.chipReturned.setOnClickListener { selectFilter(4) }
    }

    private fun showManualMatchDialog(initialReport: Report? = null) {
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

        dialogBinding.etDialogSearch.addTextChangedListener(object : TextWatcher {
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
            override fun afterTextChanged(s: Editable?) {}
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

    private fun selectFilter(index: Int) {
        selectedFilter = index
        val chips = listOf(binding.chipAll, binding.chipPending, binding.chipApprovedLost, binding.chipApprovedFound, binding.chipReturned)
        chips.forEachIndexed { i, btn ->
            if (i == index) {
                btn.setBackgroundColor("#00A884".toColorInt())
                btn.setTextColor(Color.WHITE)
            } else {
                btn.setBackgroundColor("#101726".toColorInt())
                btn.setTextColor("#9CA3AF".toColorInt())
            }
        }
        updateChipsAndList(MockRepository.reports.value)
    }

    private fun updateChipsAndList(reports: List<Report>) {
        if (_binding == null) return
        val total = reports.size
        val pending = reports.count { it.status == ReportStatus.MATCH_SUGGESTED || it.status == ReportStatus.UNDER_VERIFICATION }
        val approvedLost = reports.count { it.type == ReportType.LOST && it.status == ReportStatus.OPEN }
        val approvedFound = reports.count { it.type == ReportType.FOUND && it.status == ReportStatus.OPEN }
        val returned = reports.count { it.status == ReportStatus.RETURNED || it.status == ReportStatus.CLAIMED }

        binding.chipAll.text = "All Reports $total"
        binding.chipPending.text = "Pending Review $pending"
        binding.chipApprovedLost.text = "Approved Lost $approvedLost"
        binding.chipApprovedFound.text = "Approved Found $approvedFound"
        binding.chipReturned.text = "Returned $returned"

        val query = binding.etAdminSearch.text.toString().trim()

        val filtered = reports.filter { r ->
            val matchesQuery = query.isEmpty() ||
                    r.title.contains(query, ignoreCase = true) ||
                    r.referenceCode.contains(query, ignoreCase = true) ||
                    r.userDisplayName.contains(query, ignoreCase = true)

            val matchesChip = when (selectedFilter) {
                1 -> r.status == ReportStatus.MATCH_SUGGESTED || r.status == ReportStatus.UNDER_VERIFICATION
                2 -> r.type == ReportType.LOST && r.status == ReportStatus.OPEN
                3 -> r.type == ReportType.FOUND && r.status == ReportStatus.OPEN
                4 -> r.status == ReportStatus.RETURNED || r.status == ReportStatus.CLAIMED
                else -> true
            }

            matchesQuery && matchesChip
        }

        if (filtered.isEmpty()) {
            binding.tvAdminReportsEmpty.visibility = View.VISIBLE
            binding.rvAdminReports.visibility = View.GONE
        } else {
            binding.tvAdminReportsEmpty.visibility = View.GONE
            binding.rvAdminReports.visibility = View.VISIBLE
            adapter.submitList(filtered)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
