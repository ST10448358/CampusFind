package com.segotlo.campusfindapp.ui.xml

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.segotlo.campusfindapp.data.User
import com.segotlo.campusfindapp.data.UserRole
import com.segotlo.campusfindapp.databinding.ItemUserBinding

class UserAdapter : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvUserName.text = "${user.firstName} ${user.lastName}"
            binding.tvUserEmail.text = user.email
            binding.tvUserId.text = user.studentStaffNumber.ifEmpty { "N/A" }
            binding.tvUserDept.text = user.departmentOrFaculty.ifEmpty { "General" }

            if (user.role == UserRole.ADMIN) {
                binding.tvUserRole.text = "Admin"
                binding.tvUserRole.setBackgroundColor(Color.parseColor("#2D2354"))
                binding.tvUserRole.setTextColor(Color.parseColor("#A78BFA"))
            } else {
                binding.tvUserRole.text = "Student"
                binding.tvUserRole.setBackgroundColor(Color.parseColor("#133833"))
                binding.tvUserRole.setTextColor(Color.parseColor("#00A884"))
            }

            binding.tvUserStatus.text = "✓ Active"
            binding.tvUserStatus.setTextColor(Color.parseColor("#00A884"))
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }
}
