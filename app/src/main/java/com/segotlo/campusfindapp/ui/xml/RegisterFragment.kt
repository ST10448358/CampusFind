package com.segotlo.campusfindapp.ui.xml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.SouthAfricanInstitutions
import com.segotlo.campusfindapp.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBackReg.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvLoginPrompt.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown_white_text,
            SouthAfricanInstitutions.list
        )
        binding.actvInstitution.setAdapter(adapter)

        binding.etPasswordReg.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePasswordRequirementsUI(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Initial state update
        updatePasswordRequirementsUI(binding.etPasswordReg.text?.toString().orEmpty())

        binding.btnSubmitRegister.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val institution = binding.actvInstitution.text.toString().trim()
            val studentNum = binding.etStudentNumber.text.toString().trim()
            val email = binding.etEmailReg.text.toString().trim()
            val password = binding.etPasswordReg.text.toString().trim()
            val confirmPass = binding.etConfirmPassword.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val criteria = com.segotlo.campusfindapp.util.PasswordValidator.validate(password)
            if (!criteria.isValid) {
                binding.etPasswordReg.requestFocus()
                Toast.makeText(
                    requireContext(),
                    "Please satisfy all 5 password security requirements listed below.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPass) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!binding.cbTerms.isChecked) {
                Toast.makeText(requireContext(), "Please accept terms & conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val result = MockRepository.register(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password,
                    studentNumber = studentNum,
                    institution = institution
                )
                if (result.isSuccess) {
                    findNavController().navigate(R.id.homeFragment)
                } else {
                    Toast.makeText(requireContext(), "Registration failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updatePasswordRequirementsUI(password: String) {
        val b = _binding ?: return
        val criteria = com.segotlo.campusfindapp.util.PasswordValidator.validate(password)
        val strength = com.segotlo.campusfindapp.util.PasswordValidator.getStrength(criteria)

        val greenColor = android.graphics.Color.parseColor("#00A884")
        val grayColor = android.graphics.Color.parseColor("#9CA3AF")

        // 1. Min length (8+)
        if (criteria.hasMinLength) {
            b.tvReqMinLength.text = "✓ At least 8 characters long (${password.length}/8+)"
            b.tvReqMinLength.setTextColor(greenColor)
        } else {
            b.tvReqMinLength.text = "✕ At least 8 characters long (${password.length}/8+)"
            b.tvReqMinLength.setTextColor(grayColor)
        }

        // 2. Uppercase
        if (criteria.hasUppercase) {
            b.tvReqUppercase.text = "✓ 1 Uppercase letter (A-Z)"
            b.tvReqUppercase.setTextColor(greenColor)
        } else {
            b.tvReqUppercase.text = "✕ 1 Uppercase letter (A-Z)"
            b.tvReqUppercase.setTextColor(grayColor)
        }

        // 3. Lowercase
        if (criteria.hasLowercase) {
            b.tvReqLowercase.text = "✓ 1 Lowercase letter (a-z)"
            b.tvReqLowercase.setTextColor(greenColor)
        } else {
            b.tvReqLowercase.text = "✕ 1 Lowercase letter (a-z)"
            b.tvReqLowercase.setTextColor(grayColor)
        }

        // 4. Digit
        if (criteria.hasDigit) {
            b.tvReqDigit.text = "✓ 1 Number (0-9)"
            b.tvReqDigit.setTextColor(greenColor)
        } else {
            b.tvReqDigit.text = "✕ 1 Number (0-9)"
            b.tvReqDigit.setTextColor(grayColor)
        }

        // 5. Special char
        if (criteria.hasSpecialChar) {
            b.tvReqSpecial.text = "✓ 1 Special character (!@#$%^&*)"
            b.tvReqSpecial.setTextColor(greenColor)
        } else {
            b.tvReqSpecial.text = "✕ 1 Special character (!@#$%^&*)"
            b.tvReqSpecial.setTextColor(grayColor)
        }

        // Progress bar & badge
        b.pbPasswordStrength.progress = criteria.metCount

        when (strength) {
            com.segotlo.campusfindapp.util.PasswordStrength.VERY_WEAK -> {
                b.tvPasswordStrengthBadge.text = "VERY WEAK"
                b.tvPasswordStrengthBadge.setTextColor(android.graphics.Color.parseColor("#F87171"))
                b.pbPasswordStrength.progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F87171"))
            }
            com.segotlo.campusfindapp.util.PasswordStrength.WEAK -> {
                b.tvPasswordStrengthBadge.text = "WEAK"
                b.tvPasswordStrengthBadge.setTextColor(android.graphics.Color.parseColor("#FBBF24"))
                b.pbPasswordStrength.progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FBBF24"))
            }
            com.segotlo.campusfindapp.util.PasswordStrength.MEDIUM -> {
                b.tvPasswordStrengthBadge.text = "MEDIUM"
                b.tvPasswordStrengthBadge.setTextColor(android.graphics.Color.parseColor("#FACC15"))
                b.pbPasswordStrength.progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FACC15"))
            }
            com.segotlo.campusfindapp.util.PasswordStrength.STRONG -> {
                b.tvPasswordStrengthBadge.text = "STRONG"
                b.tvPasswordStrengthBadge.setTextColor(android.graphics.Color.parseColor("#34D399"))
                b.pbPasswordStrength.progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#34D399"))
            }
            com.segotlo.campusfindapp.util.PasswordStrength.VERY_STRONG -> {
                b.tvPasswordStrengthBadge.text = "VERY STRONG ✨"
                b.tvPasswordStrengthBadge.setTextColor(greenColor)
                b.pbPasswordStrength.progressTintList = android.content.res.ColorStateList.valueOf(greenColor)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}