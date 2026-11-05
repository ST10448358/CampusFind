package com.segotlo.campusfindapp.util

import org.junit.Assert.assertFalse
import org.junit.Test

class PasswordValidatorShortPasswordTest {

    @Test
    fun shortPasswordShouldBeRejected() {
        val result = PasswordValidator.validate("Ab1!")

        assertFalse(result.isValid)
    }
}