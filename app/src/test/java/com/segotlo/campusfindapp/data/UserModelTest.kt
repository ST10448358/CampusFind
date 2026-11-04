package com.segotlo.campusfindapp.data




import org.junit.Assert.*
import org.junit.Test

class UserModelTest {

    private fun createUser(
        role: UserRole = UserRole.STUDENT
    ): User {
        return User(
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            studentStaffNumber = "123456",
            role = role,
            departmentOrFaculty = "Computer Science"
        )
    }

    @Test
    fun userDetailsAreStoredCorrectly() {
        val user = createUser()

        assertEquals("test@example.com", user.email)
        assertEquals("Test", user.firstName)
        assertEquals("User", user.lastName)
        assertEquals("123456", user.studentStaffNumber)
        assertEquals("Computer Science", user.departmentOrFaculty)
    }

    @Test
    fun newUserHasActiveAccountByDefault() {
        val user = createUser()

        assertEquals(AccountStatus.ACTIVE, user.accountStatus)
    }

    @Test
    fun notificationsAreEnabledByDefault() {
        val user = createUser()

        assertTrue(user.pushNotificationsEnabled)
        assertTrue(user.matchAlertsEnabled)
        assertTrue(user.reportUpdatesEnabled)
    }

    @Test
    fun userCanHaveDifferentRoles() {
        val student = createUser(UserRole.STUDENT)
        val staff = createUser(UserRole.STAFF)
        val admin = createUser(UserRole.ADMIN)

        assertEquals(UserRole.STUDENT, student.role)
        assertEquals(UserRole.STAFF, staff.role)
        assertEquals(UserRole.ADMIN, admin.role)
    }

    @Test
    fun suspendedAccountCanBeCreated() {
        val user = createUser().copy(
            accountStatus = AccountStatus.SUSPENDED
        )

        assertEquals(AccountStatus.SUSPENDED, user.accountStatus)
    }

    @Test
    fun eachUserHasUniqueId() {
        val firstUser = createUser()
        val secondUser = createUser()

        assertNotEquals(firstUser.id, secondUser.id)
    }
}
