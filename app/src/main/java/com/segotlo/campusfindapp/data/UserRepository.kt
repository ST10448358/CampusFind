package com.segotlo.campusfindapp.data

import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val currentUser: StateFlow<User?>
    
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        studentNumber: String,
        institution: String,
        avatarUrl: String? = null,
        role: UserRole = UserRole.STUDENT
    ): Result<User>
    
    suspend fun logout()
    
    suspend fun updateUser(user: User): Result<Unit>
    
    fun getAllUsers(): StateFlow<List<User>>
}
