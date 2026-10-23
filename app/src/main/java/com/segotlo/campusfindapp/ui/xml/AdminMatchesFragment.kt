package com.segotlo.campusfindapp.ui.xml

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.Report
import com.segotlo.campusfindapp.data.ReportStatus
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

        lifecycleScope.launch {
            MockRepository.reports.collectLatest { reports ->
                updateMatchesUI(reports)
            }
        }

        binding.chipMatchPending.setOnClickListener { selectMatchFilter(0) }
        binding.chipMatchConfirmed.setOnClickListener { selectMatchFilter(1) }
        binding.chipMatchDismissed.setOnClickListener { selectMatchFilter(2) }
        binding.chipMatchAll.setOnClickListener { selectMatchFilter(3) }
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

                    val pair = if (report.type == com.segotlo.campusfindapp.data.ReportType.LOST) {
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
