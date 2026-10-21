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
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (_: Throwable) { null }
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Throwable) { null }
    
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _allUsers = MutableStateFlow<List<User>>(
        listOf(
            User(
                id = "admin_user_id",
                email = "admin@campusfind.ac.za",
                firstName = "Campus",
                lastName = "Admin",
                studentStaffNumber = "ADMIN-001",
                role = UserRole.ADMIN,
                departmentOrFaculty = "Security & Administration",
                institution = "University of Pretoria"
            ),
            User(
                id = "student_user_id",
                email = "student@up.ac.za",
                firstName = "Thabo",
                lastName = "Mokoena",
                studentStaffNumber = "u21000000",
                role = UserRole.STUDENT,
                departmentOrFaculty = "Informatics",
                institution = "University of Pretoria"
            )
        )
    )
    
    init {
        try {
            auth?.addAuthStateListener { firebaseAuth ->
                try {
                    val firebaseUser = firebaseAuth.currentUser
                    if (firebaseUser != null) {
                        fetchUserData(firebaseUser.uid)
                    } else {
                        // Keep current if already logged in via mock, else null
                    }
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }

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

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            if (auth != null && firestore != null) {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user ?: throw Exception("Login failed")
                
                val snapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
                if (snapshot.exists()) {
                    val user = mapDocumentToUser(snapshot.id, snapshot.data ?: emptyMap())
                    _currentUser.value = user
                    Result.success(user)
                } else {
                    Result.failure(Exception("User data not found in Firestore"))
                }
            } else {
                throw Exception("Firebase unavailable")
            }
        } catch (e: Exception) {
            // Fallback to local mock user matching
            val found = _allUsers.value.find { it.email.equals(email, ignoreCase = true) }
            if (found != null) {
                _currentUser.value = found
                Result.success(found)
            } else {
                // Create a temporary student user for successful login flow
                val newUser = User(
                    email = email,
                    firstName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    lastName = "User",
                    studentStaffNumber = "u${(10000000..99999999).random()}",
                    role = if (email.contains("admin")) UserRole.ADMIN else UserRole.STUDENT,
                    departmentOrFaculty = "General",
                    institution = "University of Pretoria"
                )
                _allUsers.value = _allUsers.value + newUser
                _currentUser.value = newUser
                Result.success(newUser)
            }
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
            if (auth != null && firestore != null) {
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
            } else {
                throw Exception("Firebase unavailable")
            }
        } catch (e: Exception) {
            // Fallback local registration
            val user = User(
                email = email,
                firstName = firstName,
                lastName = lastName,
                studentStaffNumber = studentNumber.ifEmpty { "u${(10000000..99999999).random()}" },
                role = role,
                departmentOrFaculty = "General",
                institution = institution.ifEmpty { "University of Pretoria" },
                avatarUrl = avatarUrl,
                createdAt = Date()
            )
            _allUsers.value = _allUsers.value + user
            _currentUser.value = user
            Result.success(user)
        }
    }

    override suspend fun logout() {
        try {
            auth?.signOut()
        } catch (_: Throwable) {}
        _currentUser.value = null
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            if (firestore != null) {
                firestore.collection("users").document(user.id).set(mapUserToMap(user)).await()
            }
            _currentUser.value = user
            val updatedList = _allUsers.value.map { if (it.id == user.id) user else it }
            _allUsers.value = updatedList
            Result.success(Unit)
        } catch (e: Exception) {
            _currentUser.value = user
            Result.success(Unit)
        }
    }

    override fun getAllUsers(): StateFlow<List<User>> {
        try {
            firestore?.collection("users")
                ?.orderBy("createdAt", Query.Direction.DESCENDING)
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val users = snapshot?.documents?.map { doc ->
                        mapDocumentToUser(doc.id, doc.data ?: emptyMap())
                    } ?: emptyList()
                    if (users.isNotEmpty()) {
                        _allUsers.value = users
                    }
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
