
package com.segotlo.campusfindapp.util

/**
 * Result enum representing validation outcomes for user registration input.
 */
enum class RegistrationValidationResult {
    VALID,
    REQUIRED_FIELDS_MISSING,
    INVALID_PASSWORD,
    PASSWORDS_DO_NOT_MATCH,
    TERMS_NOT_ACCEPTED
}

/**
 * Validator responsible for verifying form inputs during user account creation.
 */
object RegistrationValidator {

    /**
     * Validates user registration form fields in sequence:
     * 1. Checks that non-blank values are provided for name, email, and password.
     * 2. Verifies password satisfies security criteria via [PasswordValidator].
     * 3. Confirms matching confirmation password.
     * 4. Ensures terms of service acceptance checkbox is marked.
     *
     * @return [RegistrationValidationResult] indicating validation outcome.
     */
    fun validate(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
        termsAccepted: Boolean
    ): RegistrationValidationResult {

        if (
            firstName.isBlank() ||
            lastName.isBlank() ||
            email.isBlank() ||
            password.isBlank()
        ) {
            return RegistrationValidationResult.REQUIRED_FIELDS_MISSING
        }

        if (!PasswordValidator.validate(password).isValid) {
            return RegistrationValidationResult.INVALID_PASSWORD
        }

        if (password != confirmPassword) {
            return RegistrationValidationResult.PASSWORDS_DO_NOT_MATCH
        }

        if (!termsAccepted) {
            return RegistrationValidationResult.TERMS_NOT_ACCEPTED
        }

        return RegistrationValidationResult.VALID
    }
}
