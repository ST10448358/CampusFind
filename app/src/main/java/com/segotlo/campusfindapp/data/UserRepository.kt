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
    
    suspend fun loginWithGoogle(
        email: String,
        firstName: String,
        lastName: String,
        avatarUrl: String? = null
    ): Result<User>
    
    suspend fun checkAuthentication(): Result<User>
    
    suspend fun isUserRegistered(email: String): Boolean
    
    suspend fun logout()
    
    suspend fun updateUser(user: User): Result<Unit>
    
    fun getAllUsers(): StateFlow<List<User>>
}
