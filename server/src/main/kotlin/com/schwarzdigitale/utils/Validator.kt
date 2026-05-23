package com.schwarzdigital.utils

object Validator {
    // Regex that prevents consecutive dots, leading/trailing dots
    private val emailRegex = "^[A-Za-z0-9+_-]+(\\.[A-Za-z0-9+_-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$".toRegex()

    fun isValidEmail(email: String): Boolean {
        return email.matches(emailRegex)
    }

    fun validateEmail(email: String): ValidationResult {
        return if (isValidEmail(email)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Invalid email format")
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.length < 6 -> ValidationResult.Invalid("Password must be at least 6 characters")
            else -> ValidationResult.Valid
        }
    }

    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Invalid("Name cannot be blank")
            name.length < 2 -> ValidationResult.Invalid("Name must be at least 2 characters")
            name.length > 255 -> ValidationResult.Invalid("Name must not exceed 255 characters")
            else -> ValidationResult.Valid
        }
    }
}

sealed class ValidationResult {
    object Valid : ValidationResult()

    data class Invalid(val message: String) : ValidationResult()

    fun isValid(): Boolean = this is Valid

    fun getErrorMessage(): String? = (this as? Invalid)?.message
}
