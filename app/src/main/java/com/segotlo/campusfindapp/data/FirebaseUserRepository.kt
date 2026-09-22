package com.segotlo.campusfindapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseUserRepository : UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    
    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                fetchUserData(firebaseUser.uid)
            } else {
                _currentUser.value = null
            }
        }
    }

    private fun fetchUserData(uid: String) {
        firestore.collection("users").document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                val user = mapDocumentToUser(snapshot.id, snapshot.data ?: emptyMap())
                _currentUser.value = user
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Login failed")
            
            // Wait for snapshot listener to update _currentUser or fetch it directly once
            val snapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
            if (snapshot.exists()) {
                val user = mapDocumentToUser(snapshot.id, snapshot.data ?: emptyMap())
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("User data not found in Firestore"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        studentNumber: String,
        institution: String,
        avatarUrl: String?,
        role: UserRole
    ): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Registration failed")
            
            val user = User(
                id = firebaseUser.uid,
                email = email,
                firstName = firstName,
                lastName = lastName,
                studentStaffNumber = studentNumber,
                role = role,
                departmentOrFaculty = "General",
                institution = institution,
                avatarUrl = avatarUrl,
                createdAt = Date()
            )
            
            firestore.collection("users").document(firebaseUser.uid).set(mapUserToMap(user)).await()
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        auth.signOut()
        _currentUser.value = null
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.id).set(mapUserToMap(user)).await()
            _currentUser.value = user
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllUsers(): StateFlow<List<User>> {
        firestore.collection("users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val users = snapshot?.documents?.map { doc ->
                    mapDocumentToUser(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                _allUsers.value = users
            }
        return _allUsers.asStateFlow()
    }

    private fun mapUserToMap(user: User): Map<String, Any?> {
        return mapOf(
            "email" to user.email,
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "studentStaffNumber" to user.studentStaffNumber,
            "role" to user.role.name,
            "departmentOrFaculty" to user.departmentOrFaculty,
            "institution" to user.institution,
            "avatarUrl" to user.avatarUrl,
            "accountStatus" to user.accountStatus.name,
            "pushNotificationsEnabled" to user.pushNotificationsEnabled,
            "matchAlertsEnabled" to user.matchAlertsEnabled,
            "reportUpdatesEnabled" to user.reportUpdatesEnabled,
            "createdAt" to user.createdAt
        )
    }

    private fun mapDocumentToUser(id: String, data: Map<String, Any>): User {
        return User(
            id = id,
            email = data["email"] as? String ?: "",
            firstName = data["firstName"] as? String ?: "",
            lastName = data["lastName"] as? String ?: "",
            studentStaffNumber = data["studentStaffNumber"] as? String ?: "",
            role = UserRole.valueOf(data["role"] as? String ?: "STUDENT"),
            departmentOrFaculty = data["departmentOrFaculty"] as? String ?: "General",
            institution = data["institution"] as? String ?: "",
            avatarUrl = data["avatarUrl"] as? String,
            accountStatus = AccountStatus.valueOf(data["accountStatus"] as? String ?: "ACTIVE"),
            pushNotificationsEnabled = data["pushNotificationsEnabled"] as? Boolean ?: true,
            matchAlertsEnabled = data["matchAlertsEnabled"] as? Boolean ?: true,
            reportUpdatesEnabled = data["reportUpdatesEnabled"] as? Boolean ?: true,
            createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
        )
    }
}
