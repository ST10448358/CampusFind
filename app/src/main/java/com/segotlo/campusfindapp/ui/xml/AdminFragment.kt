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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.ReportStatus
import com.segotlo.campusfindapp.databinding.FragmentAdminPortalBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminPortalBinding? = null
    private val binding get() = _binding!!
    private lateinit var reportAdapter: ReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPortalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reportAdapter = ReportAdapter { report ->
            Toast.makeText(requireContext(), "Moderating report: ${report.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvAdminReports.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminReports.adapter = reportAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                MockRepository.reports.collectLatest { reports ->
                    val b = _binding ?: return@collectLatest
                    b.tvStatTotalReports.text = reports.size.toString()
                    val pending = reports.count { it.status == ReportStatus.MATCH_SUGGESTED || it.status == ReportStatus.UNDER_VERIFICATION }
                    b.tvStatPendingMatches.text = pending.toString()
                    val claimed = reports.count { it.status == ReportStatus.CLAIMED || it.status == ReportStatus.RETURNED }
                    b.tvStatClaimed.text = claimed.toString()

                    reportAdapter.submitList(reports)
                }
            }
        }

        binding.btnAdminLogout.setOnClickListener {
            MockRepository.logout()
            findNavController().navigate(R.id.openingFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}