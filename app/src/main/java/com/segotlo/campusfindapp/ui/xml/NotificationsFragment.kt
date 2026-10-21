package com.segotlo.campusfindapp.ui.xml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListPopupWindow
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.databinding.FragmentNotificationsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var notifAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentLang = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
        binding.btnNotifLang.text = when (currentLang) {
            "tn" -> "TN"
            "zu" -> "ZU"
            else -> "EN"
        }

        notifAdapter = NotificationAdapter()
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = notifAdapter

        lifecycleScope.launch {
            MockRepository.notifications.collectLatest { list ->
                if (list.isEmpty()) {
                    binding.cardNotifEmpty.visibility = View.VISIBLE
                    binding.rvNotifications.visibility = View.GONE
                } else {
                    binding.cardNotifEmpty.visibility = View.GONE
                    binding.rvNotifications.visibility = View.VISIBLE
                    notifAdapter.submitList(list)
                }
            }
        }

        binding.btnNotifLang.setOnClickListener { anchor ->
            val languages = arrayOf("English (EN)", "Setswana (TN)", "isiZulu (ZU)")
            val adapter = android.widget.ArrayAdapter(requireContext(), R.layout.item_dropdown_white_text, languages)
            val popup = ListPopupWindow(requireContext())
            popup.setAdapter(adapter)
            popup.anchorView = anchor
            popup.setOnItemClickListener { _, _, position, _ ->
                when (position) {
                    0 -> {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                        binding.btnNotifLang.text = "EN"
                    }
                    1 -> {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tn"))
                        binding.btnNotifLang.text = "TN"
                    }
                    2 -> {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("zu"))
                        binding.btnNotifLang.text = "ZU"
                    }
                }
                popup.dismiss()
            }
            popup.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
