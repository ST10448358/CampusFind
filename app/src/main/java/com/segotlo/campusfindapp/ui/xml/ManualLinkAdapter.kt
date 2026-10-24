package com.segotlo.campusfindapp.ui.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.Report
import com.segotlo.campusfindapp.data.ReportType
import com.segotlo.campusfindapp.databinding.ItemManualLinkReportBinding

class ManualLinkAdapter(
    private val onSelectionChanged: (List<Report>) -> Unit,
    private val onViewDetails: (Report) -> Unit
) : ListAdapter<Report, ManualLinkAdapter.ViewHolder>(DiffCallback) {

    private val selectedIds = mutableSetOf<String>()

    fun getSelectedReports(): List<Report> {
        return currentList.filter { selectedIds.contains(it.id) }
    }

    fun clearSelections() {
        selectedIds.clear()
        notifyDataSetChanged()
        onSelectionChanged(emptyList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemManualLinkReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemManualLinkReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            val context = binding.root.context
            val isSelected = selectedIds.contains(report.id)

            binding.tvReportTitle.text = report.title
            binding.tvRefCode.text = report.referenceCode
            binding.tvReportSubtitle.text = "${report.userDisplayName} • ${report.location.name}"

            if (report.type == ReportType.LOST) {
                binding.tvTypeBadge.text = "🔴 LOST"
                binding.tvTypeBadge.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_light))
            } else {
                binding.tvTypeBadge.text = "🟢 FOUND"
                binding.tvTypeBadge.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_light))
            }

            // Remove previous listener before setting checked state to avoid triggers
            binding.cbSelectReport.setOnCheckedChangeListener(null)
            binding.cbSelectReport.isChecked = isSelected

            binding.cbSelectReport.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (selectedIds.size >= 2) {
                        binding.cbSelectReport.isChecked = false
                        Toast.makeText(context, "You can only select up to 2 reports to link", Toast.LENGTH_SHORT).show()
                        return@setOnCheckedChangeListener
                    }
                    selectedIds.add(report.id)
                } else {
                    selectedIds.remove(report.id)
                }
                onSelectionChanged(getSelectedReports())
            }

            binding.root.setOnClickListener {
                binding.cbSelectReport.performClick()
            }

            binding.btnViewDetails.setOnClickListener {
                onViewDetails(report)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean =
            oldItem == newItem
    }
}
