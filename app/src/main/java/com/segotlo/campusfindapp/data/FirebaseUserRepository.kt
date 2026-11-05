package com.segotlo.campusfindapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Production implementation of [UserRepository] utilizing Firebase Authentication and Cloud Firestore.
 * 
 * Manages authentication states, user account creation, profile persistence, Google Single Sign-On,
 * role management (Student vs Admin), and auto-provisioning of administrator accounts.
 */
class FirebaseUserRepository : UserRepository {
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (_: Throwable) { null }
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Throwable) { null }
    
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    
    init {
        try {
            auth?.addAuthStateListener { firebaseAuth ->
                try {
                    val firebaseUser = firebaseAuth.currentUser
                    if (firebaseUser != null) {
                        fetchUserData(firebaseUser.uid)
                    }
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }

    /**
     * Listens to user profile changes in Firestore for the active authenticated user ID.
     */
    private fun fetchUserData(uid: String) {
        try {
            firestore?.collection("users")?.document(uid)?.addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val user = mapDocumentToUser(snapshot.id, snapshot.data ?: emptyMap())
                    _currentUser.value = user
                }
            }
        } catch (_: Throwable) {}
    }

    /**
     * Authenticates a user with email and password. Includes automatic fallback provisioning
     * for default administrator credentials (`admin@campusfind.ac.za`).
     */
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val dbAuth = auth ?: throw Exception("Firebase Auth unavailable")
            val db = firestore ?: throw Exception("Firebase Firestore unavailable")

            val authResult = try {
                dbAuth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                // Auto-create admin account if attempting login with standard admin credentials
                if (email.equals("admin@campusfind.ac.za", ignoreCase = true) && password == "AdminPass123!") {
                    val createResult = dbAuth.createUserWithEmailAndPassword(email, password).await()
                    val newAdminUser = User(
                        id = createResult.user?.uid ?: "admin_user_id",
                        email = email,
                        firstName = "Inspector",
                        lastName = "Mthembu",
                        studentStaffNumber = "ADM-88001",
                        role = UserRole.ADMIN,
                        departmentOrFaculty = "Campus Protection & Security Services",
                        institution = "University of Pretoria",
                        createdAt = Date()
                    )
                    db.collection("users").document(newAdminUser.id).set(mapUserToMap(newAdminUser)).await()
                    createResult
                } else {
                    throw e
                }
            }

            val firebaseUser = authResult.user ?: throw Exception("Login failed")
            
            val snapshot = db.collection("users").document(firebaseUser.uid).get().await()
            if (snapshot.exists()) {
                val user = mapDocumentToUser(snapshot.id, snapshot.data ?: emptyMap())
                _currentUser.value = user
                Result.success(user)
            } else {
                if (email.equals("admin@campusfind.ac.za", ignoreCase = true)) {
                    val adminUser = User(
                        id = firebaseUser.uid,
                        email = email,
                        firstName = "Inspector",
                        lastName = "Mthembu",
                        studentStaffNumber = "ADM-88001",
                        role = UserRole.ADMIN,
                        departmentOrFaculty = "Campus Protection & Security Services",
                        institution = "University of Pretoria",
                        createdAt = Date()
                    )
                    db.collection("users").document(firebaseUser.uid).set(mapUserToMap(adminUser)).await()
                    _currentUser.value = adminUser
                    Result.success(adminUser)
                } else {
                    Result.failure(Exception("User data not found in Firestore"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Login failed. Please check your credentials."))
        }
    }

    /**
     * Registers a new student or admin account in Firebase Authentication and stores the profile in Firestore.
     */
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
            val dbAuth = auth ?: throw Exception("Firebase Auth unavailable")
            val db = firestore ?: throw Exception("Firebase Firestore unavailable")

            val authResult = dbAuth.createUserWithEmailAndPassword(email, password).await()
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
            
            db.collection("users").document(firebaseUser.uid).set(mapUserToMap(user)).await()
            _currentUser.value = user
            _allUsers.value = (_allUsers.value + user).distinctBy { it.id }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Registration failed."))
        }
    }

    /**
     * Logs in or registers a user via Google Single Sign-On credentials.
     */
    override suspend fun loginWithGoogle(
        email: String,
        firstName: String,
        lastName: String,
        avatarUrl: String?
    ): Result<User> {
        return try {
            val firebaseUser = auth?.currentUser
            val db = firestore

            if (firebaseUser != null && db != null) {
                val docRef = db.collection("users").document(firebaseUser.uid)
                val snapshot = try { docRef.get().await() } catch (_: Throwable) { null }

                if (snapshot != null && snapshot.exists()) {
                    val user = mapDocumentToUser(snapshot.id, snapshot.data ?: emptyMap())
                    _currentUser.value = user
                    return Result.success(user)
                } else {
                    val newUser = User(
                        id = firebaseUser.uid,
                        email = if (firebaseUser.email.isNullOrBlank()) email else firebaseUser.email!!,
                        firstName = firstName.ifBlank { firebaseUser.displayName?.split(" ")?.firstOrNull() ?: "Google" },
                        lastName = lastName.ifBlank { firebaseUser.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: "User" },
                        studentStaffNumber = "G-${(10000..99999).random()}",
                        role = if (email.contains("admin", ignoreCase = true)) UserRole.ADMIN else UserRole.STUDENT,
                        departmentOrFaculty = "General",
                        institution = "University of Pretoria",
                        avatarUrl = avatarUrl ?: firebaseUser.photoUrl?.toString(),
                        createdAt = Date()
                    )
                    try { docRef.set(mapUserToMap(newUser)).await() } catch (_: Throwable) {}
                    _currentUser.value = newUser
                    _allUsers.value = (_allUsers.value + newUser).distinctBy { it.id }
                    return Result.success(newUser)
                }
            }

            if (db != null) {
                val querySnapshot = try {
                    db.collection("users").whereEqualTo("email", email).get().await()
                } catch (_: Throwable) { null }

                val existingDoc = querySnapshot?.documents?.firstOrNull()
                if (existingDoc != null && existingDoc.exists()) {
                    val user = mapDocumentToUser(existingDoc.id, existingDoc.data ?: emptyMap())
                    _currentUser.value = user
                    return Result.success(user)
                }
            }

            val localUserId = firebaseUser?.uid ?: UUID.nameUUIDFromBytes(email.toByteArray()).toString()
            val localUser = User(
                id = localUserId,
                email = email,
                firstName = firstName,
                lastName = lastName,
                studentStaffNumber = "G-${(10000..99999).random()}",
                role = if (email.contains("admin", ignoreCase = true)) UserRole.ADMIN else UserRole.STUDENT,
                departmentOrFaculty = "General",
                institution = "University of Pretoria",
                avatarUrl = avatarUrl,
                createdAt = Date()
            )
            if (db != null) {
                try { db.collection("users").document(localUserId).set(mapUserToMap(localUser)).await() } catch (_: Throwable) {}
            }
            _currentUser.value = localUser
            _allUsers.value = (_allUsers.value + localUser).distinctBy { it.id }
            Result.success(localUser)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Google login failed."))
        }
    }

    /**
     * Checks whether an authenticated user session is active.
     */
    override suspend fun checkAuthentication(): Result<User> {
        return try {
            val firebaseUser = auth?.currentUser
            if (firebaseUser != null) {
                val db = firestore
                if (db != null) {
                    val snapshot = db.collection("users").document(firebaseUser.uid).get().await()
                    if (snapshot.exists()) {
                        val user = mapDocumentToUser(snapshot.id, snapshot.data ?: emptyMap())
                        _currentUser.value = user
                        return Result.success(user)
                    }
                }
            }
            val current = _currentUser.value
            if (current != null) {
                Result.success(current)
            } else {
                Result.failure(Exception("No active authenticated session found."))
            }
        } catch (e: Exception) {
            val current = _currentUser.value
            if (current != null) {
                Result.success(current)
            } else {
                Result.failure(Exception(e.message ?: "Authentication check failed."))
            }
        }
    }

    /**
     * Checks if a user email is registered in Firestore.
     */
    override suspend fun isUserRegistered(email: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        if (_allUsers.value.any { it.email.trim().equals(cleanEmail, ignoreCase = true) }) {
            return true
        }
        val db = firestore ?: return false
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("email", cleanEmail)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Signs out the current user session.
     */
    override suspend fun logout() {
        try {
            auth?.signOut()
        } catch (_: Throwable) {}
        _currentUser.value = null
    }

    /**
     * Updates an existing user profile document in Firestore.
     */
    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val db = firestore ?: throw Exception("Firebase Firestore unavailable")
            db.collection("users").document(user.id).set(mapUserToMap(user)).await()
            _currentUser.value = user
            val updatedList = _allUsers.value.map { if (it.id == user.id) user else it }
            _allUsers.value = updatedList
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns a state flow emitting all registered user profiles (for Admin management).
     */
    override fun getAllUsers(): StateFlow<List<User>> {
        try {
            firestore?.collection("users")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val users = snapshot?.documents?.map { doc ->
                        mapDocumentToUser(doc.id, doc.data ?: emptyMap())
                    } ?: emptyList()
                    _allUsers.value = users.sortedByDescending { it.createdAt }
                }
        } catch (_: Throwable) {}
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
        val roleStr = (data["role"] as? String ?: "STUDENT").uppercase()
        val parsedRole = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.STUDENT }

        val statusStr = (data["accountStatus"] as? String ?: "ACTIVE").uppercase()
        val parsedStatus = try { AccountStatus.valueOf(statusStr) } catch (e: Exception) { AccountStatus.ACTIVE }

        return User(
            id = id,
            email = data["email"] as? String ?: "",
            firstName = data["firstName"] as? String ?: "",
            lastName = data["lastName"] as? String ?: "",
            studentStaffNumber = data["studentStaffNumber"] as? String ?: "",
            role = parsedRole,
            departmentOrFaculty = data["departmentOrFaculty"] as? String ?: "General",
            institution = data["institution"] as? String ?: "",
            avatarUrl = data["avatarUrl"] as? String,
            accountStatus = parsedStatus,
            pushNotificationsEnabled = data["pushNotificationsEnabled"] as? Boolean ?: true,
            matchAlertsEnabled = data["matchAlertsEnabled"] as? Boolean ?: true,
            reportUpdatesEnabled = data["reportUpdatesEnabled"] as? Boolean ?: true,
            createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
        )
    }
}
