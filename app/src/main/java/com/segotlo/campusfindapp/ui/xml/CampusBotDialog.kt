package com.segotlo.campusfindapp.ui.xml

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
        val words = lower.split(Regex("\\s+"))

        val reply = when {
            words.contains("hello") || words.contains("hi") || words.contains("hey") ->
                "Hello there! I'm CampusBot AI. I'm here to help you navigate lost and found reports, claims, and collection desks. How can I help you today?"

            words.contains("bye") || words.contains("goodbye") || lower.contains("see you") ->
                "Goodbye! Have a wonderful day on campus. Feel free to reach out anytime you need assistance finding or returning lost items!"

            lower.contains("thank") ->
                "You're very welcome! I'm always here to help you out. Let me know if you need anything else."

            // Specific match & meeting inquiries first!
            lower.contains("get it") || lower.contains("exchange") || lower.contains("meet") || lower.contains("handover") || lower.contains("pin") || lower.contains("code") || lower.contains("someone found") || lower.contains("got a message") ->
                "Once an admin confirms your match, you will receive a secure 4-digit Handover PIN in your 'My Reports' tab. You and the finder can meet at the Campus Main Gate Desk, where security will verify your PIN and safely return your item."

            lower.contains("status") || lower.contains("track") || lower.contains("my report") ->
                "You can track the progress of any item you've submitted by visiting the 'My Reports' tab in the bottom menu. There you can see if your report is open, matched, or claimed."

            lower.contains("proof") || lower.contains("claim") ->
                "To claim an item, submit a claim with proof of ownership (like serial numbers or matching description) and correctly answer the secret security question set by the finder."

            lower.contains("collect") || lower.contains("where") || lower.contains("desk") || lower.contains("location") ->
                "Items can be collected at the Campus Main Gate Desk (Extension 4400) upon security verification and ID inspection."

            lower.contains("match") || lower.contains("ai") ->
                "CampusFind uses an AI-assisted verification bridge that automatically pairs lost and found reports based on category, location, and keywords for admin review."

            lower.contains("find") || lower.contains("search") || lower.contains("locate") || lower.contains("charger") || lower.contains("phone") || lower.contains("keys") || lower.contains("bag") || lower.contains("laptop") ->
                "To find your item (like a charger, phone, or keys), use the Search tab in the app. You can search by keyword or filter by category and location. If someone has reported finding it, our AI matching engine will also automatically suggest a match!"

            // General reporting rules checked after specific match/claiming questions
            lower.contains("lost") || lower.contains("missing") ->
                "To report a lost item, tap 'Report Lost' on the Home screen. Fill in the item title, category, location pin on the map, and a secret identifier (like your wallpaper or scratch mark) so campus security can verify your ownership."

            lower.contains("found") ->
                "To report a found item, tap 'Report Found' on the Home screen. Provide details of where you found it and hand it in at the Campus Main Gate Desk."

            lower.contains("help") || lower.contains("assist") ->
                "I'm here to help! You can ask me how to report a lost item, how to report a found item, how AI matching works, what proof is needed to claim, or where to collect items."

            else ->
                "I hear you asking about \"$query\". While my core focus is on campus lost & found reporting, AI matching, claiming, and collection desks (Ext: 4400), you can ask me how to report a lost item, search for missing belongings, or check your report status!"
        }

        binding.rvChatMessages.postDelayed({
            if (_binding != null) {
                addBotMessage(reply)
            }
        }, 400)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
