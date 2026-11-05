
package com.segotlo.campusfindapp.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RegistrationValidatorAdditionalTest {

    @Test
    fun mismatchedPasswordsShouldReturnValid() {

        val result = RegistrationValidator.validate(
            firstName = "Lesedi",
            lastName = "Mathunya",
            email = "student@example.com",
            password = "CampusFind9!",
            confirmPassword = "Different123!",
            termsAccepted = true
        )

        // Intentionally incorrect expectation
        assertEquals(
            RegistrationValidationResult.VALID,
            result
        )
    }
}
