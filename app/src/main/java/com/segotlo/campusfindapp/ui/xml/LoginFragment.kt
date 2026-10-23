package com.segotlo.campusfindapp.ui.xml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.UserRole
import com.segotlo.campusfindapp.databinding.FragmentLoginBinding
import com.segotlo.campusfindapp.util.GoogleAuthManager
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvRegisterPrompt.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }

        binding.btnUseAdmin.setOnClickListener {
            binding.etEmail.setText("admin@campusfind.ac.za")
            binding.etPassword.setText("AdminPass123!")
        }

        binding.btnSubmitLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val result = MockRepository.login(email, password)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user?.role == UserRole.ADMIN) {
                        findNavController().navigate(R.id.adminDashboardFragment)
                    } else {
                        findNavController().navigate(R.id.homeFragment)
                    }
                } else {
                    Toast.makeText(requireContext(), "Login failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnGoogleSignin.setOnClickListener {
            lifecycleScope.launch {
                val result = GoogleAuthManager.signInWithGoogle(requireContext())
                if (result.isSuccess) {
                    findNavController().navigate(R.id.homeFragment)
                } else {
                    val err = result.exceptionOrNull()?.message ?: "Unknown error"
                    Toast.makeText(requireContext(), "Google sign-in failed: $err", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}