package com.segotlo.campusfindapp.ui.xml

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.segotlo.campusfindapp.databinding.ItemChatMessageBinding

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatViewHolder(private val binding: ItemChatMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            if (message.isUser) {
                binding.llUserBubble.visibility = View.VISIBLE
                binding.llBotBubble.visibility = View.GONE
                binding.tvUserMessage.text = message.text
            } else {
                binding.llUserBubble.visibility = View.GONE
                binding.llBotBubble.visibility = View.VISIBLE
                binding.tvBotMessage.text = message.text
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem == newItem
    }
}
