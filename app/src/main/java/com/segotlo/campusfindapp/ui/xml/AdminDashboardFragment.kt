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
import com.segotlo.campusfindapp.data.ReportStatus
import com.segotlo.campusfindapp.data.ReportType
import com.segotlo.campusfindapp.databinding.FragmentAdminDashboardBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reportAdapter = ReportAdapter {}
        binding.rvAdminDashboardReports.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminDashboardReports.adapter = reportAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                MockRepository.reports.collectLatest { reports ->
                    val b = _binding ?: return@collectLatest
                    val pending = reports.count { it.status == ReportStatus.MATCH_SUGGESTED || it.status == ReportStatus.UNDER_VERIFICATION }
                    val lost = reports.count { it.type == ReportType.LOST }
                    val found = reports.count { it.type == ReportType.FOUND }
                    val matches = reports.count { it.status == ReportStatus.MATCH_SUGGESTED }
                    val returned = reports.count { it.status == ReportStatus.RETURNED || it.status == ReportStatus.CLAIMED }

                    b.tvStatPending.text = pending.toString()
                    b.tvStatLost.text = lost.toString()
                    b.tvStatFound.text = found.toString()
                    b.tvStatMatches.text = matches.toString()
                    b.tvStatReturned.text = returned.toString()

                    b.tvReviewBannerTitle.text = "$pending possible matches awaiting review"

                    if (reports.isEmpty()) {
                        b.llAdminDashboardEmpty.visibility = View.VISIBLE
                        b.rvAdminDashboardReports.visibility = View.GONE
                    } else {
                        b.llAdminDashboardEmpty.visibility = View.GONE
                        b.rvAdminDashboardReports.visibility = View.VISIBLE
                        reportAdapter.submitList(reports.take(3))
                    }
                }
            }
        }



        binding.tvReviewNow.setOnClickListener {
            findNavController().navigate(R.id.adminMatchesFragment)
        }

        binding.tvAdminViewAll.setOnClickListener {
            findNavController().navigate(R.id.adminReportsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
