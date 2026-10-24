package com.segotlo.campusfindapp.ui.xml

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.User
import com.segotlo.campusfindapp.data.UserRole
import com.segotlo.campusfindapp.databinding.FragmentAdminUsersBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminUsersFragment : Fragment() {
    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: UserAdapter

    private var selectedRoleFilter = 0 // 0: All, 1: Students, 2: Admins
    private var selectedStatusFilter = 0 // 0: All Status, 1: Active, 2: Deactivated

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UserAdapter()
        binding.rvAdminUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminUsers.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                MockRepository.getAllUsers().collectLatest { users ->
                    if (_binding != null) {
                        updateUI(users)
                    }
                }
            }
        }

        binding.etUserSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUI(MockRepository.getAllUsers().value)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipRoleAll.setOnClickListener { selectRoleFilter(0) }
        binding.chipRoleStudents.setOnClickListener { selectRoleFilter(1) }
        binding.chipRoleAdmins.setOnClickListener { selectRoleFilter(2) }

        binding.chipStatusAll.setOnClickListener { selectStatusFilter(0) }
        binding.chipStatusActive.setOnClickListener { selectStatusFilter(1) }
        binding.chipStatusDeactivated.setOnClickListener { selectStatusFilter(2) }


    }

    private fun selectRoleFilter(index: Int) {
        selectedRoleFilter = index
        val chips = listOf(binding.chipRoleAll, binding.chipRoleStudents, binding.chipRoleAdmins)
        chips.forEachIndexed { i, btn ->
            if (i == index) {
                btn.setBackgroundColor(Color.parseColor("#00A884"))
                btn.setTextColor(Color.WHITE)
            } else {
                btn.setBackgroundColor(Color.parseColor("#101726"))
                btn.setTextColor(Color.parseColor("#9CA3AF"))
            }
        }
        updateUI(MockRepository.getAllUsers().value)
    }

    private fun selectStatusFilter(index: Int) {
        selectedStatusFilter = index
        val chips = listOf(binding.chipStatusAll, binding.chipStatusActive, binding.chipStatusDeactivated)
        chips.forEachIndexed { i, btn ->
            if (i == index) {
                btn.setBackgroundColor(Color.parseColor("#00A884"))
                btn.setTextColor(Color.WHITE)
            } else {
                btn.setBackgroundColor(Color.parseColor("#101726"))
                btn.setTextColor(Color.parseColor("#9CA3AF"))
            }
        }
        updateUI(MockRepository.getAllUsers().value)
    }

    private fun updateUI(users: List<User>) {
        if (_binding == null) return
        val total = users.size
        val students = users.count { it.role == UserRole.STUDENT }
        val admins = users.count { it.role == UserRole.ADMIN }

        binding.chipRoleAll.text = "All ($total)"
        binding.chipRoleStudents.text = "Students ($students)"
        binding.chipRoleAdmins.text = "Admins ($admins)"
        binding.chipStatusActive.text = "Active ($total)"
        binding.chipStatusDeactivated.text = "Deactivated (0)"

        val query = binding.etUserSearch.text.toString().trim()

        val filtered = users.filter { u ->
            val name = "${u.firstName} ${u.lastName}"
            val matchesQuery = query.isEmpty() ||
                    name.contains(query, ignoreCase = true) ||
                    u.email.contains(query, ignoreCase = true) ||
                    u.studentStaffNumber.contains(query, ignoreCase = true)

            val matchesRole = when (selectedRoleFilter) {
                1 -> u.role == UserRole.STUDENT
                2 -> u.role == UserRole.ADMIN
                else -> true
            }

            matchesQuery && matchesRole
        }

        if (filtered.isEmpty()) {
            binding.tvAdminUsersEmpty.visibility = View.VISIBLE
            binding.rvAdminUsers.visibility = View.GONE
        } else {
            binding.tvAdminUsersEmpty.visibility = View.GONE
            binding.rvAdminUsers.visibility = View.VISIBLE
            adapter.submitList(filtered)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
