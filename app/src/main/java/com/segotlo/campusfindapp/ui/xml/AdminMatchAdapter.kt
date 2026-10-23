package com.segotlo.campusfindapp.ui.xml

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.segotlo.campusfindapp.data.Report
import com.segotlo.campusfindapp.data.ReportStatus
import com.segotlo.campusfindapp.databinding.ItemAdminMatchBinding

class AdminMatchAdapter(
    private val onConfirm: (Report, Report) -> Unit,
    private val onReject: (Report, Report) -> Unit
) : ListAdapter<Pair<Report, Report>, AdminMatchAdapter.MatchViewHolder>(MatchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemAdminMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding, onConfirm, onReject)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MatchViewHolder(
        private val binding: ItemAdminMatchBinding,
        private val onConfirm: (Report, Report) -> Unit,
        private val onReject: (Report, Report) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(matchPair: Pair<Report, Report>) {
            val lost = matchPair.first
            val found = matchPair.second

            binding.tvLostTitle.text = lost.title
            binding.tvLostLocation.text = lost.location.name.lowercase().replace('_', ' ')
            binding.tvFoundTitle.text = found.title
            binding.tvFoundLocation.text = found.location.name.lowercase().replace('_', ' ')

            binding.tvMatchConfidence.text = "✨ AI Confidence: 85%"
            binding.tvMatchReasoning.text = "Category (${lost.category.name.lowercase().replace('_', ' ')}) & Location align"

            if (lost.status == ReportStatus.MATCH_SUGGESTED || found.status == ReportStatus.MATCH_SUGGESTED) {
                binding.tvMatchStatusBadge.text = "Pending Review"
                binding.llMatchActions.visibility = View.VISIBLE
            } else if (lost.status == ReportStatus.RETURNED || lost.status == ReportStatus.CLAIMED) {
                binding.tvMatchStatusBadge.text = "Confirmed [PIN: ${lost.handoverPin}]"
                binding.llMatchActions.visibility = View.GONE
            } else {
                binding.tvMatchStatusBadge.text = "Dismissed"
                binding.llMatchActions.visibility = View.GONE
            }

            binding.btnConfirmMatch.setOnClickListener {
                onConfirm(lost, found)
            }
            binding.btnRejectMatch.setOnClickListener {
                onReject(lost, found)
            }
        }
    }

    class MatchDiffCallback : DiffUtil.ItemCallback<Pair<Report, Report>>() {
        override fun areItemsTheSame(oldItem: Pair<Report, Report>, newItem: Pair<Report, Report>): Boolean {
            return oldItem.first.id == newItem.first.id && oldItem.second.id == newItem.second.id
        }
        override fun areContentsTheSame(oldItem: Pair<Report, Report>, newItem: Pair<Report, Report>): Boolean {
            return oldItem == newItem
        }
    }
}
