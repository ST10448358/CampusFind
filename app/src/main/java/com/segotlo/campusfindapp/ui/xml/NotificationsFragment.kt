package com.segotlo.campusfindapp.ui.xml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListPopupWindow
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

        notifAdapter = NotificationAdapter { notification ->
            handleNotificationClick(notification)
        }
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = notifAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                MockRepository.notifications.collectLatest { list ->
                    val b = _binding ?: return@collectLatest
                    if (list.isEmpty()) {
                        b.cardNotifEmpty.visibility = View.VISIBLE
                        b.rvNotifications.visibility = View.GONE
                    } else {
                        b.cardNotifEmpty.visibility = View.GONE
                        b.rvNotifications.visibility = View.VISIBLE
                        notifAdapter.submitList(list)
                    }
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

    private fun handleNotificationClick(notification: com.segotlo.campusfindapp.data.AppNotification) {
        MockRepository.markNotificationAsRead(notification.id)

        val reportsList = MockRepository.reports.value
        val report = if (notification.reportId != null) {
            reportsList.find { it.id == notification.reportId }
        } else {
            val user = MockRepository.currentUser.value
            if (user != null) reportsList.find { it.userId == user.id } else null
        } ?: reportsList.firstOrNull()

        if (report != null) {
            showHandoverInfoDialog(report)
        } else {
            android.widget.Toast.makeText(requireContext(), notification.message, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun showHandoverInfoDialog(report: com.segotlo.campusfindapp.data.Report) {
        val dialogBinding = com.segotlo.campusfindapp.databinding.DialogHandoverInfoBinding.inflate(layoutInflater)
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvHandoverReportTitle.text = report.title
        dialogBinding.tvHandoverRefCode.text = "${report.referenceCode} • Verification Info"
        dialogBinding.tvHandoverStatus.text = report.status.name

        val formattedPin = report.handoverPin.toCharArray().joinToString(" ")
        dialogBinding.tvHandoverPin.text = formattedPin

        dialogBinding.tvHandoverLocation.text = "Campus Main Gate Desk • ${report.location.name}"
        dialogBinding.tvHandoverInstitution.text = report.institution

        dialogBinding.btnCopyPin.setOnClickListener {
            val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Handover PIN", report.handoverPin)
            clipboard.setPrimaryClip(clip)
            android.widget.Toast.makeText(requireContext(), "PIN ${report.handoverPin} copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
        }

        dialogBinding.btnCloseHandoverDialog.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDoneHandover.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
