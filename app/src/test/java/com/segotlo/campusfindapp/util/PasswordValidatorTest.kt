package com.segotlo.campusfindapp.util




import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidatorTest {

    @Test
    fun validPasswordMeetsAllCriteria() {
        val result = PasswordValidator.validate("CampusFind9!")

        assertTrue(result.hasMinLength)
        assertTrue(result.hasUppercase)
        assertTrue(result.hasLowercase)
        assertTrue(result.hasDigit)
        assertTrue(result.hasSpecialChar)
        assertTrue(result.isValid)
        assertEquals(5, result.metCount)
    }

    @Test
    fun shortPasswordFailsMinimumLength() {
        val result = PasswordValidator.validate("Ab1!")

        assertFalse(result.hasMinLength)
        assertFalse(result.isValid)
    }

    @Test
    fun passwordWithoutUppercaseFails() {
        val result = PasswordValidator.validate("campusfind9!")

        assertFalse(result.hasUppercase)
        assertFalse(result.isValid)
    }

    @Test
    fun passwordWithoutLowercaseFails() {
        val result = PasswordValidator.validate("CAMPUSFIND9!")

        assertFalse(result.hasLowercase)
        assertFalse(result.isValid)
    }

    @Test
    fun passwordWithoutDigitFails() {
        val result = PasswordValidator.validate("CampusFind!")

        assertFalse(result.hasDigit)
        assertFalse(result.isValid)
    }

    @Test
    fun passwordWithoutSpecialCharacterFails() {
        val result = PasswordValidator.validate("CampusFind9")

        assertFalse(result.hasSpecialChar)
        assertFalse(result.isValid)
    }

    @Test
    fun emptyPasswordFailsAllCriteria() {
        val result = PasswordValidator.validate("")

        assertFalse(result.isValid)
        assertEquals(0, result.metCount)
        assertEquals(
            PasswordStrength.VERY_WEAK,
            PasswordValidator.getStrength(result)
        )
    }

    @Test
    fun passwordStrengthMatchesNumberOfCriteriaMet() {
        assertEquals(
            PasswordStrength.VERY_WEAK,
            PasswordValidator.getStrength(
                PasswordCriteria(false, false, false, false, false)
            )
        )

        assertEquals(
            PasswordStrength.WEAK,
            PasswordValidator.getStrength(
                PasswordCriteria(true, true, false, false, false)
            )
        )

        assertEquals(
            PasswordStrength.MEDIUM,
            PasswordValidator.getStrength(
                PasswordCriteria(true, true, true, false, false)
            )
        )

        assertEquals(
            PasswordStrength.STRONG,
            PasswordValidator.getStrength(
                PasswordCriteria(true, true, true, true, false)
            )
        )

        assertEquals(
            PasswordStrength.VERY_STRONG,
            PasswordValidator.getStrength(
                PasswordCriteria(true, true, true, true, true)
            )
        )
    }
}
