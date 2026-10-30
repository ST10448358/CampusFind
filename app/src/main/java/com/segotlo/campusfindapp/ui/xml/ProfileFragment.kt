package com.segotlo.campusfindapp.ui.xml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.ReportStatus
import com.segotlo.campusfindapp.data.ReportType
import com.segotlo.campusfindapp.databinding.DialogChangePasswordBinding
import com.segotlo.campusfindapp.databinding.DialogEditProfileBinding
import com.segotlo.campusfindapp.databinding.DialogPrivacyPolicyBinding
import com.segotlo.campusfindapp.databinding.DialogTermsUseBinding
import com.segotlo.campusfindapp.databinding.FragmentProfileBinding
import com.segotlo.campusfindapp.util.UriUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val safeUri = UriUtils.copyUriToInternalFile(requireContext(), uri, "avatar_gallery")
                if (safeUri != null) {
                    binding.imgProfileAvatar.imageTintList = null
                    binding.imgProfileAvatar.setImageURI(safeUri)
                    val currentUser = MockRepository.currentUser.value
                    if (currentUser != null) {
                        MockRepository.updateUser(currentUser.copy(avatarUrl = safeUri.toString()))
                    }
                    Toast.makeText(requireContext(), "Profile photo updated from Gallery", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile photo", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Failed to update profile photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCameraPreview()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            try {
                val file = java.io.File(requireContext().cacheDir, "avatar_camera_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }
                val fileUri = android.net.Uri.fromFile(file)

                binding.imgProfileAvatar.imageTintList = null
                binding.imgProfileAvatar.setImageURI(fileUri)

                val currentUser = MockRepository.currentUser.value
                if (currentUser != null) {
                    MockRepository.updateUser(currentUser.copy(avatarUrl = fileUri.toString()))
                }
                Toast.makeText(requireContext(), "Profile photo updated from Camera", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                binding.imgProfileAvatar.imageTintList = null
                binding.imgProfileAvatar.setImageBitmap(bitmap)
                Toast.makeText(requireContext(), "Profile photo updated from Camera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchCameraPreview() {
        try {
            cameraLauncher.launch(null)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "Unable to open camera app", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize radio button states based on current app locale
        val locales = AppCompatDelegate.getApplicationLocales()
        val currentLang = if (!locales.isEmpty) locales.get(0)?.language ?: "en" else "en"
        binding.radioLangEn.isChecked = currentLang == "en" || currentLang.isEmpty()
        binding.radioLangTn.isChecked = currentLang == "tn"
        binding.radioLangZu.isChecked = currentLang == "zu"

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                MockRepository.currentUser.collectLatest { user ->
                    val b = _binding ?: return@collectLatest
                    if (user != null) {
                        b.tvProfileName.text = "${user.firstName} ${user.lastName}"
                        b.tvProfileEmail.text = user.email
                        b.tvProfileStudentNum.text = "Student #${user.studentStaffNumber}"

                        b.switchPushNotif.isChecked = user.pushNotificationsEnabled
                        b.switchMatchAlerts.isChecked = user.matchAlertsEnabled
                        b.switchReportUpdates.isChecked = user.reportUpdatesEnabled

                        if (!user.avatarUrl.isNullOrEmpty()) {
                            try {
                                val avatarUri = android.net.Uri.parse(user.avatarUrl)
                                if (avatarUri.scheme == "content") {
                                    val safeUri = UriUtils.copyUriToInternalFile(requireContext(), avatarUri, "avatar_cache")
                                    if (safeUri != null) {
                                        b.imgProfileAvatar.imageTintList = null
                                        b.imgProfileAvatar.setImageURI(safeUri)
                                        MockRepository.updateUser(user.copy(avatarUrl = safeUri.toString()))
                                    } else {
                                        b.imgProfileAvatar.setImageResource(R.drawable.ic_nav_profile)
                                    }
                                } else if (avatarUri.scheme == "file" || avatarUri.scheme.isNullOrEmpty()) {
                                    b.imgProfileAvatar.imageTintList = null
                                    val safeFileUri = if (avatarUri.scheme.isNullOrEmpty()) android.net.Uri.fromFile(java.io.File(user.avatarUrl)) else avatarUri
                                    b.imgProfileAvatar.setImageURI(safeFileUri)
                                } else {
                                    b.imgProfileAvatar.imageTintList = null
                                    b.imgProfileAvatar.setImageURI(avatarUri)
                                }
                            } catch (_: Throwable) {
                                b.imgProfileAvatar.setImageResource(R.drawable.ic_nav_profile)
                            }
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                MockRepository.reports.collectLatest { reports ->
                    val b = _binding ?: return@collectLatest
                    val user = MockRepository.currentUser.value
                    val userReports = if (user != null) reports.filter { it.userId == user.id } else reports
                    
                    val lostCount = userReports.count { it.type == ReportType.LOST }
                    val foundCount = userReports.count { it.type == ReportType.FOUND }
                    val returnedCount = userReports.count { it.status == ReportStatus.RETURNED || it.status == ReportStatus.CLAIMED }

                    b.tvStatLostCount.text = lostCount.toString()
                    b.tvStatFoundCount.text = foundCount.toString()
                    b.tvStatReturnedCount.text = returnedCount.toString()
                }
            }
        }

        binding.switchPushNotif.setOnCheckedChangeListener { _, isChecked ->
            val user = MockRepository.currentUser.value ?: return@setOnCheckedChangeListener
            MockRepository.updateUser(user.copy(pushNotificationsEnabled = isChecked))
        }

        binding.switchMatchAlerts.setOnCheckedChangeListener { _, isChecked ->
            val user = MockRepository.currentUser.value ?: return@setOnCheckedChangeListener
            MockRepository.updateUser(user.copy(matchAlertsEnabled = isChecked))
        }

        binding.switchReportUpdates.setOnCheckedChangeListener { _, isChecked ->
            val user = MockRepository.currentUser.value ?: return@setOnCheckedChangeListener
            MockRepository.updateUser(user.copy(reportUpdatesEnabled = isChecked))
        }

        // Camera / Avatar click
        binding.containerAvatar.setOnClickListener { showImageSourceDialog() }
        binding.imgProfileAvatar.setOnClickListener { showImageSourceDialog() }

        // Language selection with AppCompatDelegate app-wide locales
        binding.radioLangEn.setOnClickListener {
            binding.radioLangEn.isChecked = true
            binding.radioLangTn.isChecked = false
            binding.radioLangZu.isChecked = false
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            Toast.makeText(requireContext(), "Language set to English", Toast.LENGTH_SHORT).show()
        }
        binding.radioLangTn.setOnClickListener {
            binding.radioLangEn.isChecked = false
            binding.radioLangTn.isChecked = true
            binding.radioLangZu.isChecked = false
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tn"))
            Toast.makeText(requireContext(), "Puo e beilwe go Setswana", Toast.LENGTH_SHORT).show()
        }
        binding.radioLangZu.setOnClickListener {
            binding.radioLangEn.isChecked = false
            binding.radioLangTn.isChecked = false
            binding.radioLangZu.isChecked = true
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("zu"))
            Toast.makeText(requireContext(), "Ulimi lubekwe isiZulu", Toast.LENGTH_SHORT).show()
        }

        // Dark mode switch
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            MockRepository.setDarkTheme(isChecked)
            Toast.makeText(requireContext(), if (isChecked) "Dark Mode Enabled" else "Light Mode Enabled", Toast.LENGTH_SHORT).show()
        }

        // Row clicks
        binding.rowEditProfile.setOnClickListener { showEditProfileDialog() }
        binding.rowChangePassword.setOnClickListener { showChangePasswordDialog() }
        binding.rowPrivacyPolicy.setOnClickListener { showPrivacyPolicyDialog() }
        binding.rowTermsUse.setOnClickListener { showTermsOfUseDialog() }
        binding.rowHelpSupport.setOnClickListener {
            Toast.makeText(requireContext(), "Campus Support: security@campusfind.ac.za | Room 102", Toast.LENGTH_LONG).show()
        }

        binding.btnLogout.setOnClickListener {
            MockRepository.logout()
            findNavController().navigate(R.id.openingFragment)
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo with Camera", "Choose from Gallery")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Profile Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                            requireContext(),
                            android.Manifest.permission.CAMERA
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            launchCameraPreview()
                        } else {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    }
                    1 -> {
                        try {
                            galleryLauncher.launch("image/*")
                        } catch (_: Exception) {
                            Toast.makeText(requireContext(), "Gallery not available", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showEditProfileDialog() {
        val dialogBinding = DialogEditProfileBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        val user = MockRepository.currentUser.value
        if (user != null) {
            dialogBinding.etEditFirstName.setText(user.firstName)
            dialogBinding.etEditLastName.setText(user.lastName)
            dialogBinding.etEditEmail.setText(user.email)
            dialogBinding.etEditStudentNum.setText(user.studentStaffNumber)
        }

        dialogBinding.btnDialogClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnDialogCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSaveProfile.setOnClickListener {
            val newFirst = dialogBinding.etEditFirstName.text.toString().trim()
            val newLast = dialogBinding.etEditLastName.text.toString().trim()
            val newEmail = dialogBinding.etEditEmail.text.toString().trim()
            val newStudentNum = dialogBinding.etEditStudentNum.text.toString().trim()

            if (user != null && newFirst.isNotEmpty() && newLast.isNotEmpty()) {
                MockRepository.updateUser(
                    user.copy(
                        firstName = newFirst,
                        lastName = newLast,
                        email = newEmail.ifEmpty { user.email },
                        studentStaffNumber = newStudentNum.ifEmpty { user.studentStaffNumber }
                    )
                )
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please fill in required fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
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
                Toast.makeText(requireContext(), "New password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newP != confirmP) {
                Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(requireContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show()
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
