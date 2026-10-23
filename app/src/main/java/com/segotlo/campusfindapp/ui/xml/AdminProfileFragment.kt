package com.segotlo.campusfindapp.ui.xml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.databinding.DialogChangePasswordBinding
import com.segotlo.campusfindapp.databinding.DialogPrivacyPolicyBinding
import com.segotlo.campusfindapp.databinding.DialogTermsUseBinding
import com.segotlo.campusfindapp.databinding.FragmentAdminProfileBinding

class AdminProfileFragment : Fragment() {
    private var _binding: FragmentAdminProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.switchAdminDarkMode.isChecked = MockRepository.isDarkTheme.value
        binding.switchAdminDarkMode.setOnCheckedChangeListener { _, isChecked ->
            MockRepository.setDarkTheme(isChecked)
            Toast.makeText(requireContext(), if (isChecked) "Dark Mode Enabled" else "Light Mode Enabled", Toast.LENGTH_SHORT).show()
        }

        binding.rowAdminChangePassword.setOnClickListener { showChangePasswordDialog() }
        binding.rowAdminPrivacy.setOnClickListener { showPrivacyPolicyDialog() }
        binding.rowAdminTerms.setOnClickListener { showTermsOfUseDialog() }

        binding.btnAdminProfileLogout.setOnClickListener {
            MockRepository.logout()
            findNavController().navigate(R.id.openingFragment)
        }
    }

    private fun showChangePasswordDialog() {
        val dialogBinding = DialogChangePasswordBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnDialogClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDialogCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSavePassword.setOnClickListener {
            val curr = dialogBinding.etCurrPassword.text.toString()
            val newP = dialogBinding.etNewPassword.text.toString()
            val confirmP = dialogBinding.etConfirmPassword.text.toString()

            if (curr.isEmpty() || newP.isEmpty() || confirmP.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all password fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newP.length < 8) {
                Toast.makeText(requireContext(), "Admin password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newP != confirmP) {
                Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(requireContext(), "Admin password updated successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showPrivacyPolicyDialog() {
        val dialogBinding = DialogPrivacyPolicyBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnDialogClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showTermsOfUseDialog() {
        val dialogBinding = DialogTermsUseBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnDialogClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
