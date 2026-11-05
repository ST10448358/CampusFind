package com.segotlo.campusfindapp.ui.xml

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.exceptions.GetCredentialCancellationException
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

        binding.btnSubmitLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                show6SecondPopUpMessage("Please enter both email and password")
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
                    val msg = result.exceptionOrNull()?.message ?: "Login failed"
                    show6SecondPopUpMessage("Login failed: $msg")
                }
            }
        }

        binding.btnGoogleSignin.setOnClickListener {
            lifecycleScope.launch {
                val result = GoogleAuthManager.signInWithGoogle(requireActivity())
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    show6SecondPopUpMessage("Welcome back to CampusFind, ${user?.firstName}!")
                    if (user?.role == UserRole.ADMIN) {
                        findNavController().navigate(R.id.adminDashboardFragment)
                    } else {
                        findNavController().navigate(R.id.homeFragment)
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    if (exception is GoogleAuthManager.UnregisteredAccountException) {
                        val email = exception.email
                        show6SecondPopUpMessage("No account registered for $email. Please register first to access CampusFind.")
                        val bundle = Bundle().apply { putString("prefill_email", email) }
                        findNavController().navigate(R.id.registerFragment, bundle)
                    } else if (exception !is GetCredentialCancellationException) {
                        val err = exception?.message ?: "Google sign-in error"
                        show6SecondPopUpMessage(err)
                    }
                }
            }
        }
    }

    private fun show6SecondPopUpMessage(message: String) {
        val ctx = context ?: return
        Toast.makeText(ctx, message, Toast.LENGTH_LONG).show()

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                if (isAdded && context != null) {
                    Toast.makeText(ctx, message, Toast.LENGTH_LONG).show()
                }
            } catch (_: Throwable) {}
        }, 3000L)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
