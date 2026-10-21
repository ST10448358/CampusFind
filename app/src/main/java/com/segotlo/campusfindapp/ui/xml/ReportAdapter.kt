package com.segotlo.campusfindapp.ui.xml

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.segotlo.campusfindapp.data.Report
import com.segotlo.campusfindapp.data.ReportType
import com.segotlo.campusfindapp.databinding.ItemReportBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ReportAdapter(
    private val onItemClick: (Report) -> Unit
) : ListAdapter<Report, ReportAdapter.ReportViewHolder>(ReportDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReportViewHolder(private val binding: ItemReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            binding.tvItemTitle.text = report.title
            binding.tvItemDesc.text = report.description
            binding.tvItemLocation.text = "📍 ${report.institution} • ${report.location.name}"

            val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            binding.tvItemDate.text = sdf.format(report.dateOccurred)

            if (report.type == ReportType.LOST) {
                binding.tvItemBadge.text = "LOST"
                binding.tvItemBadge.setTextColor(Color.parseColor("#818CF8"))
                binding.tvItemBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2D2354"))
            } else {
                binding.tvItemBadge.text = "FOUND"
                binding.tvItemBadge.setTextColor(Color.parseColor("#00A884"))
                binding.tvItemBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0F332A"))
            }

            binding.root.setOnClickListener { onItemClick(report) }
        }
    }

    class ReportDiffCallback : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean = oldItem == newItem
    }
}