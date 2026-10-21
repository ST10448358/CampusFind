package com.segotlo.campusfindapp.ui.xml

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.databinding.FragmentOpeningBinding

class OpeningFragment : Fragment() {

    private var _binding: FragmentOpeningBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOpeningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentLang = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
        val initialSelected = when (currentLang) {
            "tn" -> 1
            "zu" -> 2
            else -> 0
        }
        updateLangUI(initialSelected)

        binding.btnGetStarted.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }

        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }

        binding.btnLangEn.setOnClickListener { updateLangUI(0) }
        binding.btnLangTn.setOnClickListener { updateLangUI(1) }
        binding.btnLangZu.setOnClickListener { updateLangUI(2) }
    }

    private fun updateLangUI(selected: Int) {
        binding.btnLangEn.setBackgroundColor(if (selected == 0) Color.WHITE else Color.TRANSPARENT)
        binding.btnLangEn.setTextColor(if (selected == 0) Color.parseColor("#0B0F19") else Color.parseColor("#9CA3AF"))

        binding.btnLangTn.setBackgroundColor(if (selected == 1) Color.WHITE else Color.TRANSPARENT)
        binding.btnLangTn.setTextColor(if (selected == 1) Color.parseColor("#0B0F19") else Color.parseColor("#9CA3AF"))

        binding.btnLangZu.setBackgroundColor(if (selected == 2) Color.WHITE else Color.TRANSPARENT)
        binding.btnLangZu.setTextColor(if (selected == 2) Color.parseColor("#0B0F19") else Color.parseColor("#9CA3AF"))

        when (selected) {
            0 -> {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                binding.tvOpeningSubtitle.text = getString(R.string.tagline)
                binding.btnGetStarted.text = getString(R.string.get_started)
                binding.btnLogin.text = getString(R.string.log_in)
            }
            1 -> {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tn"))
                binding.tvOpeningSubtitle.text = getString(R.string.tagline)
                binding.btnGetStarted.text = getString(R.string.get_started)
                binding.btnLogin.text = getString(R.string.log_in)
            }
            2 -> {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("zu"))
                binding.tvOpeningSubtitle.text = getString(R.string.tagline)
                binding.btnGetStarted.text = getString(R.string.get_started)
                binding.btnLogin.text = getString(R.string.log_in)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
