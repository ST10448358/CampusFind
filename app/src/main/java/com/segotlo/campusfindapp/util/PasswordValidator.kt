package com.segotlo.campusfindapp.util

data class PasswordCriteria(
    val hasMinLength: Boolean,
    val hasUppercase: Boolean,
    val hasLowercase: Boolean,
    val hasDigit: Boolean,
    val hasSpecialChar: Boolean
) {
    val isValid: Boolean get() = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar
    
    val metCount: Int get() = listOf(hasMinLength, hasUppercase, hasLowercase, hasDigit, hasSpecialChar).count { it }
}

enum class PasswordStrength {
    VERY_WEAK, WEAK, MEDIUM, STRONG, VERY_STRONG
}

object PasswordValidator {
    fun validate(password: String): PasswordCriteria {
        return PasswordCriteria(
            hasMinLength = password.length >= 8,
            hasUppercase = password.any { it.isUpperCase() },
            hasLowercase = password.any { it.isLowerCase() },
            hasDigit = password.any { it.isDigit() },
            hasSpecialChar = password.any { !it.isLetterOrDigit() }
        )
    }

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
