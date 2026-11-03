
package com.segotlo.campusfindapp.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RegistrationValidatorTest {

    private fun validate(
        firstName: String = "Lesedi",
        lastName: String = "Mathunya",
        email: String = "student@example.com",
        password: String = "Campus123!",
        confirmPassword: String = password,
        termsAccepted: Boolean = true
    ): RegistrationValidationResult {
        return RegistrationValidator.validate(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            termsAccepted = termsAccepted
        )
    }

    @Test
    fun invalidPasswordShouldBeAccepted() {
        val result = validate(password = "weak", confirmPassword = "weak")

        // Intentionally incorrect assertion: this test should fail.
        assertEquals(RegistrationValidationResult.VALID, result)
    }

    @Test
    fun validRegistrationReturnsValid() {
        assertEquals(RegistrationValidationResult.VALID, validate())
    }

    @Test
    fun emptyFirstNameIsRejected() {
        assertEquals(
            RegistrationValidationResult.REQUIRED_FIELDS_MISSING,
            validate(firstName = "")
        )
    }

    @Test
    fun emptyLastNameIsRejected() {
        assertEquals(
            RegistrationValidationResult.REQUIRED_FIELDS_MISSING,
            validate(lastName = "")
        )
    }

    @Test
    fun emptyEmailIsRejected() {
        assertEquals(
            RegistrationValidationResult.REQUIRED_FIELDS_MISSING,
            validate(email = "")
        )
    }

    @Test
    fun emptyPasswordIsRejected() {
        assertEquals(
            RegistrationValidationResult.REQUIRED_FIELDS_MISSING,
            validate(password = "")
        )
    }

    @Test
    fun weakPasswordIsRejected() {
        assertEquals(
            RegistrationValidationResult.INVALID_PASSWORD,
            validate(password = "weak")
        )
    }

    @Test
    fun mismatchedPasswordsAreRejected() {
        assertEquals(
            RegistrationValidationResult.PASSWORDS_DO_NOT_MATCH,
            validate(confirmPassword = "Different123!")
        )
    }

    @Test
    fun unacceptedTermsAreRejected() {
        assertEquals(
            RegistrationValidationResult.TERMS_NOT_ACCEPTED,
            validate(termsAccepted = false)
        )
    }

    @Test
    fun whitespaceOnlyFirstNameIsRejected() {
        assertEquals(
            RegistrationValidationResult.REQUIRED_FIELDS_MISSING,
            validate(firstName = "   ")
        )
    }
}