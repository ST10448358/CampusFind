package com.segotlo.campusfindapp.ui.xml

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.Report
import com.segotlo.campusfindapp.data.ReportType
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

        lifecycleScope.launch {
            MockRepository.currentUser.collectLatest { user ->
                if (user != null) {
                    binding.tvUsername.text = "${user.firstName} ${user.lastName}"
                    binding.tvUserId.text = "ID: ${user.studentStaffNumber}"
                }
            }
        }

        lifecycleScope.launch {
            MockRepository.reports.collectLatest { reports ->
                filterReports(binding.etHomeSearch.text.toString(), reports)
            }
        }

        binding.etHomeSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterReports(s.toString(), MockRepository.reports.value)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

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

        binding.btnAlertsBell.setOnClickListener {
            findNavController().navigate(R.id.notificationsFragment)
        }

        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        binding.btnLanguageDropdown.setOnClickListener { v ->
            showLanguagePopup(v)
        }

        binding.btnThemeToggle.setOnClickListener {
            val isDark = MockRepository.isDarkTheme.value
            MockRepository.setDarkTheme(!isDark)
            binding.btnThemeToggle.text = if (!isDark) "Dark" else "Light"
        }
    }

    private fun filterReports(query: String, reports: List<Report>) {
        val filtered = if (query.isBlank()) reports else {
            reports.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.institution.contains(query, ignoreCase = true)
            }
        }

        if (filtered.isEmpty()) {
            binding.tvNoReports.visibility = View.VISIBLE
            binding.rvHomeReports.visibility = View.GONE
        } else {
            binding.tvNoReports.visibility = View.GONE
            binding.rvHomeReports.visibility = View.VISIBLE
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
        dialogBinding.tvDetailContact.text = "Reporter: ${report.userDisplayName} (${report.userEmail})"

        dialogBinding.btnDetailClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDetailClaim.setOnClickListener {
            Toast.makeText(requireContext(), "Contact request sent to ${report.userDisplayName}!", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showLanguagePopup(anchor: View) {
        val languages = arrayOf("English (EN)", "Setswana (TN)", "isiZulu (ZU)")
        val adapter = android.widget.ArrayAdapter(requireContext(), R.layout.item_dropdown_white_text, languages)
        val popup = ListPopupWindow(requireContext())
        popup.setAdapter(adapter)
        popup.anchorView = anchor
        popup.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    binding.tvHomeTagline.text = "| Find what you lost. Return wh..."
                    binding.btnLanguageDropdown.text = "EN"
                }
                1 -> {
                    binding.tvHomeTagline.text = "| Bona se o se latlhegetsweng..."
                    binding.btnLanguageDropdown.text = "TN"
                }
                2 -> {
                    binding.tvHomeTagline.text = "| Thola eng okulahlekileyo..."
                    binding.btnLanguageDropdown.text = "ZU"
                }
            }
            popup.dismiss()
        }
        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}