package com.segotlo.campusfindapp.util

import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidatorFailureDemoTest {

    @Test
    fun passwordContainingDigitDemonstratesCorrectExpectation() {
        val result = PasswordValidator.validate("CampusFind9!")

        // The password contains a digit, so hasDigit should be true.
        assertTrue(result.hasDigit)
    }
}
