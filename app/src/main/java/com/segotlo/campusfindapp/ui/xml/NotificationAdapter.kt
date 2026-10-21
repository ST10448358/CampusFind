package com.segotlo.campusfindapp.ui.xml

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.segotlo.campusfindapp.data.AppNotification
import com.segotlo.campusfindapp.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter : ListAdapter<AppNotification, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppNotification) {
            binding.tvNotifTitle.text = item.title
            binding.tvNotifMessage.text = item.message
            val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            binding.tvNotifTime.text = sdf.format(item.timestamp)
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<AppNotification>() {
        override fun areItemsTheSame(oldItem: AppNotification, newItem: AppNotification): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AppNotification, newItem: AppNotification): Boolean = oldItem == newItem
    }
}