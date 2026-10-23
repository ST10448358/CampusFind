package com.segotlo.campusfindapp.ui.xml

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.segotlo.campusfindapp.databinding.DialogCampusBotBinding

class CampusBotDialog : BottomSheetDialogFragment() {

    private var _binding: DialogCampusBotBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCampusBotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatAdapter = ChatAdapter()
        binding.rvChatMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvChatMessages.adapter = chatAdapter

        if (messages.isEmpty()) {
            addBotMessage("Hello! I'm CampusBot AI. How can I assist you with reporting, claiming, or collecting items today?")
        } else {
            chatAdapter.submitList(messages.toList())
        }

        binding.btnCloseChat.setOnClickListener { dismiss() }
        binding.btnClearChat.setOnClickListener {
            messages.clear()
            addBotMessage("Chat history cleared. How else can I assist you today?")
        }

        binding.btnSendChat.setOnClickListener {
            val text = binding.etChatInput.text.toString().trim()
            if (text.isNotEmpty()) {
                binding.etChatInput.setText("")
                handleUserQuery(text)
            }
        }

        binding.chipQ1.setOnClickListener { handleUserQuery("How do I report a lost item?") }
        binding.chipQ2.setOnClickListener { handleUserQuery("How do I report a found item?") }
        binding.chipQ3.setOnClickListener { handleUserQuery("What proof is needed to claim?") }
        binding.chipQ4.setOnClickListener { handleUserQuery("Where do I collect my item?") }
    }

    private fun addUserMessage(text: String) {
        messages.add(ChatMessage(text, true))
        chatAdapter.submitList(messages.toList()) {
            binding.rvChatMessages.scrollToPosition(messages.size - 1)
        }
    }

    private fun addBotMessage(text: String) {
        messages.add(ChatMessage(text, false))
        chatAdapter.submitList(messages.toList()) {
            binding.rvChatMessages.scrollToPosition(messages.size - 1)
        }
    }

    private fun handleUserQuery(query: String) {
        addUserMessage(query)

        val lower = query.lowercase()
        val reply = when {
            lower.contains("lost") -> "To report a lost item, tap 'Report Lost' on the Home screen. Fill in the item title, category, location pin on the map, and a secret identifier (like your wallpaper or scratch mark) so campus security can verify your ownership."
            lower.contains("found") -> "To report a found item, tap 'Report Found' on the Home screen. Provide details of where you found it and hand it in at the Campus Main Gate Desk."
            lower.contains("proof") || lower.contains("claim") -> "To claim an item, submit a claim with proof of ownership (like serial numbers or matching description) and correctly answer the secret security question set by the finder."
            lower.contains("collect") || lower.contains("where") || lower.contains("desk") -> "Items can be collected at the Campus Main Gate Desk (Extension 4400) upon security verification and ID inspection."
            lower.contains("match") || lower.contains("ai") -> "CampusFind uses an AI-assisted verification bridge that automatically pairs lost and found reports based on category, location, and keywords for admin review."
            else -> "I can assist you with reporting lost/found items, AI matching, claiming procedures, and collection desks. Feel free to tap one of the quick questions above!"
        }

        binding.rvChatMessages.postDelayed({
            if (_binding != null) {
                addBotMessage(reply)
            }
        }, 500)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
