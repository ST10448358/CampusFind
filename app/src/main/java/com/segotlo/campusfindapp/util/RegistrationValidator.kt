
package com.segotlo.campusfindapp.util


enum class RegistrationValidationResult {
    VALID,
    REQUIRED_FIELDS_MISSING,
    INVALID_PASSWORD,
    PASSWORDS_DO_NOT_MATCH,
    TERMS_NOT_ACCEPTED
}

object RegistrationValidator {

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
