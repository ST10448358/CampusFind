package com.segotlo.campusfindapp.ui.xml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.Report
import com.segotlo.campusfindapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var reportAdapter: ReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reportAdapter = ReportAdapter { report ->
            showReportDetailDialog(report)
        }
        binding.rvHomeReports.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHomeReports.adapter = reportAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                MockRepository.currentUser.collectLatest { user ->
                    val b = _binding ?: return@collectLatest
                    if (user != null) {
                        b.tvUsername.text = "${user.firstName} ${user.lastName}"
                        b.tvUserId.text = "ID: ${user.studentStaffNumber}"
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                MockRepository.reports.collectLatest { reports ->
                    if (_binding != null) {
                        filterReports(reports)
                    }
                }
            }
        }

        binding.cardProfileSearch.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        binding.cardReportLost.setOnClickListener {
            val bundle = Bundle().apply { putString("report_type", "LOST") }
            findNavController().navigate(R.id.reportItemFragment, bundle)
        }

        binding.cardReportFound.setOnClickListener {
            val bundle = Bundle().apply { putString("report_type", "FOUND") }
            findNavController().navigate(R.id.reportItemFragment, bundle)
        }

        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }
    }

    private fun filterReports(reports: List<Report>) {
        val b = _binding ?: return
        if (reports.isEmpty()) {
            b.tvNoReports.visibility = View.VISIBLE
            b.rvHomeReports.visibility = View.GONE
        } else {
            b.tvNoReports.visibility = View.GONE
            b.rvHomeReports.visibility = View.VISIBLE
            reportAdapter.submitList(reports.take(3))
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
        dialogBinding.tvDetailContact.text = "Reporter: ${report.userDisplayName} (${report.userEmail})"

        dialogBinding.btnDetailClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDetailClaim.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Contact request sent to ${report.userDisplayName}!", android.widget.Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
