package com.segotlo.campusfindapp.ui.xml

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.CampusLocation
import com.segotlo.campusfindapp.data.ItemCategory
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.Report
import com.segotlo.campusfindapp.data.ReportType
import com.segotlo.campusfindapp.databinding.FragmentSearchBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var reportAdapter: ReportAdapter

    private var selectedCategory: ItemCategory? = null
    private var selectedLocation: CampusLocation? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reportAdapter = ReportAdapter { report ->
            showReportDetailDialog(report)
        }
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = reportAdapter

        lifecycleScope.launch {
            MockRepository.reports.collectLatest {
                applyFilters()
            }
        }

        binding.etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.tabsSearchType.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                applyFilters()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.btnFilterDialog.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun applyFilters() {
        val query = binding.etSearchQuery.text.toString().trim()
        val allReports = MockRepository.reports.value

        val typeFilter = when (binding.tabsSearchType.selectedTabPosition) {
            1 -> ReportType.LOST
            2 -> ReportType.FOUND
            else -> null
        }

        val filtered = allReports.filter { r ->
            (typeFilter == null || r.type == typeFilter) &&
            (selectedCategory == null || r.category == selectedCategory) &&
            (selectedLocation == null || r.location == selectedLocation) &&
            (query.isEmpty() || r.title.contains(query, ignoreCase = true) ||
                    r.description.contains(query, ignoreCase = true) ||
                    r.institution.contains(query, ignoreCase = true))
        }

        binding.tvActiveFilterSummary.text = "Showing: ${selectedCategory?.name ?: "All Categories"} • ${selectedLocation?.name ?: "All Locations"}"

        if (filtered.isEmpty()) {
            binding.tvSearchEmpty.visibility = View.VISIBLE
            binding.rvSearchResults.visibility = View.GONE
        } else {
            binding.tvSearchEmpty.visibility = View.GONE
            binding.rvSearchResults.visibility = View.VISIBLE
            reportAdapter.submitList(filtered)
        }
    }

    private fun showFilterDialog() {
        val filterBinding = com.segotlo.campusfindapp.databinding.DialogFilterReportsBinding.inflate(layoutInflater)
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(filterBinding.root)
            .create()

        val catNames = listOf("All Categories") + ItemCategory.values().map { it.name }
        filterBinding.actvFilterCategory.setAdapter(ArrayAdapter(requireContext(), R.layout.item_dropdown_white_text, catNames))

        val locNames = listOf("All Locations") + CampusLocation.values().map { it.name }
        filterBinding.actvFilterLocation.setAdapter(ArrayAdapter(requireContext(), R.layout.item_dropdown_white_text, locNames))

        filterBinding.btnApplyFilters.setOnClickListener {
            val catText = filterBinding.actvFilterCategory.text.toString()
            selectedCategory = ItemCategory.values().find { it.name == catText }

            val locText = filterBinding.actvFilterLocation.text.toString()
            selectedLocation = CampusLocation.values().find { it.name == locText }

            applyFilters()
            dialog.dismiss()
        }

        dialog.show()
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
            Toast.makeText(requireContext(), "Claim request sent for '${report.title}'!", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}