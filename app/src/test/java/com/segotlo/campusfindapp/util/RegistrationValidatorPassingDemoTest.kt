
package com.segotlo.campusfindapp.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RegistrationValidatorPassingDemoTest {

    @Test
    fun validRegistrationShouldReturnValid() {
        val result = RegistrationValidator.validate(
            firstName = "Lesedi",
            lastName = "Mathunya",
            email = "student@example.com",
            password = "Campus123!",
            confirmPassword = "Campus123!",
            termsAccepted = true
        )

        assertEquals(
            RegistrationValidationResult.VALID,
            result
        )
    }
}
