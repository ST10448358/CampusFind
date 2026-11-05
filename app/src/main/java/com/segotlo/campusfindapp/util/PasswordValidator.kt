package com.segotlo.campusfindapp.util

/**
 * Data container evaluating individual security criteria for a password candidate.
 *
 * @property hasMinLength True if password length is 8 or more characters.
 * @property hasUppercase True if contains at least one uppercase letter (A-Z).
 * @property hasLowercase True if contains at least one lowercase letter (a-z).
 * @property hasDigit True if contains at least one numeric digit (0-9).
 * @property hasSpecialChar True if contains at least one special character (!@#$%^&* etc.).
 */
data class PasswordCriteria(
    val hasMinLength: Boolean,
    val hasUppercase: Boolean,
    val hasLowercase: Boolean,
    val hasDigit: Boolean,
    val hasSpecialChar: Boolean
) {
    /** True if all 5 security criteria are satisfied. */
    val isValid: Boolean get() = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar
    
    /** Total number of met security criteria (0 to 5). */
    val metCount: Int get() = listOf(hasMinLength, hasUppercase, hasLowercase, hasDigit, hasSpecialChar).count { it }
}

/**
 * Enumeration of password strength categories.
 */
enum class PasswordStrength {
    VERY_WEAK, WEAK, MEDIUM, STRONG, VERY_STRONG
}

/**
 * Utility validator for evaluating password security rules and strength scores.
 */
object PasswordValidator {

    /**
     * Evaluates a password string against defined security rules.
     *
     * @param password Raw input password.
     * @return [PasswordCriteria] specifying status for each required criterion.
     */
    fun validate(password: String): PasswordCriteria {
        return PasswordCriteria(
            hasMinLength = password.length >= 8,
            hasUppercase = password.any { it.isUpperCase() },
            hasLowercase = password.any { it.isLowerCase() },
            hasDigit = password.any { it.isDigit() },
            hasSpecialChar = password.any { !it.isLetterOrDigit() }
        )
    }

    /**
     * Derives an overall [PasswordStrength] level from the number of met criteria.
     *
     * @param criteria Evaluated [PasswordCriteria].
     * @return Calculated strength rating.
     */
    fun getStrength(criteria: PasswordCriteria): PasswordStrength {
        return when (criteria.metCount) {
            0, 1 -> PasswordStrength.VERY_WEAK
            2 -> PasswordStrength.WEAK
            3 -> PasswordStrength.MEDIUM
            4 -> PasswordStrength.STRONG
            5 -> PasswordStrength.VERY_STRONG
            else -> PasswordStrength.VERY_WEAK
        }
    }
}
