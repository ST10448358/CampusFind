package com.segotlo.campusfindapp.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidatorAdditionalTest {

    @Test
    fun validPasswordShouldPassValidation() {
        val result = PasswordValidator.validate("CampusFind9!")

        assertTrue(result.isValid)
        assertEquals(
            PasswordStrength.VERY_STRONG,
            PasswordValidator.getStrength(result)
        )
    }
}