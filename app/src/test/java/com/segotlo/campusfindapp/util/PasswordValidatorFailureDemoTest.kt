
package com.segotlo.campusfindapp.util

import org.junit.Assert.assertFalse
import org.junit.Test

class PasswordValidatorFailureDemoTest {

    @Test
    fun passwordContainingDigitDemonstratesIncorrectExpectation() {
        val result = PasswordValidator.validate("CampusFind9!")

        // This expectation is deliberately incorrect.
        // The password contains a digit, so hasDigit should be true.
        assertFalse(result.hasDigit)
    }
}
