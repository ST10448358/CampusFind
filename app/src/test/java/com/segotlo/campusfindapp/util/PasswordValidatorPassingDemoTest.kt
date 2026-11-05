package com.segotlo.campusfindapp.util

import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidatorPassingDemoTest {

    @Test
    fun passwordContainingDigitIsValidatedCorrectly() {
        val result = PasswordValidator.validate("CampusFind9!")

        // Check that a password containing a digit is validated correctly.
        assertTrue(result.hasDigit)
    }
}
