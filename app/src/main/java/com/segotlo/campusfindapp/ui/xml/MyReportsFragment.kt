package com.segotlo.campusfindapp.ui.xml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.Report
import com.segotlo.campusfindapp.data.ReportStatus
import com.segotlo.campusfindapp.data.ReportType
import com.segotlo.campusfindapp.databinding.FragmentMyReportsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyReportsFragment : Fragment() {

    private var _binding: FragmentMyReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var reportAdapter: ReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reportAdapter = ReportAdapter { report ->
            showReportDetailDialog(report)
        }
        binding.rvMyReports.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyReports.adapter = reportAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                MockRepository.currentUser.collectLatest { user ->
                    val b = _binding ?: return@collectLatest
                    if (user != null) {
                        MockRepository.getReportsByUser(user.id).collectLatest { userReports ->
                            applyTabFilter(userReports)
                        }
                    } else {
                        b.tvMyReportsEmpty.visibility = View.VISIBLE
                        b.rvMyReports.visibility = View.GONE
                    }
                }
            }
        }

        binding.tabsMyReports.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val user = MockRepository.currentUser.value
                if (user != null) {
                    applyTabFilter(MockRepository.getReportsByUser(user.id).value)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun applyTabFilter(reports: List<Report>) {
        val b = _binding ?: return
        val filtered = when (b.tabsMyReports.selectedTabPosition) {
            1 -> reports.filter { it.type == ReportType.LOST }
            2 -> reports.filter { it.type == ReportType.FOUND }
            3 -> reports.filter { it.status == ReportStatus.CLAIMED || it.status == ReportStatus.RETURNED }
            else -> reports
        }

        if (filtered.isEmpty()) {
            b.tvMyReportsEmpty.visibility = View.VISIBLE
            b.rvMyReports.visibility = View.GONE
        } else {
            b.tvMyReportsEmpty.visibility = View.GONE
            b.rvMyReports.visibility = View.VISIBLE
            reportAdapter.submitList(filtered)
        }
    }

    private fun showReportDetailDialog(report: Report) {
        val dialogBinding = com.segotlo.campusfindapp.databinding.DialogReportDetailBinding.inflate(layoutInflater)
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvDetailTitle.text = report.title
        dialogBinding.tvDetailDesc.text = report.description
        dialogBinding.tvDetailLocation.text = "📍 ${report.institution} • ${report.location.name}"
        
        val pinInfo = if (report.matchedWithId != null) "\n\n🔐 Secure Handover PIN: ${report.handoverPin}\n📍 Handover Desk: Campus Main Gate Desk" else ""
        dialogBinding.tvDetailContact.text = "Status: ${report.status.name}$pinInfo"

        if (report.matchedWithId != null) {
            dialogBinding.btnDetailClaim.text = "View Handover PIN & Exchange Location"
            dialogBinding.btnDetailClaim.setOnClickListener {
                dialog.dismiss()
                showHandoverInfoDialog(report)
            }
        } else {
            dialogBinding.btnDetailClaim.setOnClickListener {
                Toast.makeText(requireContext(), "Report status: ${report.status.name}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialogBinding.btnDetailClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showHandoverInfoDialog(report: Report) {
        val dialogBinding = com.segotlo.campusfindapp.databinding.DialogHandoverInfoBinding.inflate(layoutInflater)
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvHandoverReportTitle.text = report.title
        dialogBinding.tvHandoverRefCode.text = "${report.referenceCode} • Verification Info"
        dialogBinding.tvHandoverStatus.text = report.status.name

        val formattedPin = report.handoverPin.toCharArray().joinToString(" ")
        dialogBinding.tvHandoverPin.text = formattedPin

        dialogBinding.tvHandoverLocation.text = "Campus Main Gate Desk • ${report.location.name}"
        dialogBinding.tvHandoverInstitution.text = report.institution

        dialogBinding.btnCopyPin.setOnClickListener {
            val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Handover PIN", report.handoverPin)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "PIN ${report.handoverPin} copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        dialogBinding.btnCloseHandoverDialog.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDoneHandover.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
