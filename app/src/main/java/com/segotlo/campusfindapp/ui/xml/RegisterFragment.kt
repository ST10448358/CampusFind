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

            if (password != confirmPass) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val criteria = com.segotlo.campusfindapp.util.PasswordValidator.validate(password)
            if (!criteria.isValid) {
                Toast.makeText(
                    requireContext(),
                    "Password must be at least 8 characters with 1 uppercase, 1 lowercase, 1 digit, and 1 special character.",
                    Toast.LENGTH_LONG
                ).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}