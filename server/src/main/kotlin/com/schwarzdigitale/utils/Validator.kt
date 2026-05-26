package com.schwarzdigital.utils

object Validator {
    // Constants
    private const val MIN_PUBLISHING_YEAR = 1000
    private const val MAX_PUBLISHING_YEAR = 2100

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

    fun validateBookTitle(title: String): ValidationResult {
        return when {
            title.isBlank() -> ValidationResult.Invalid("Book title cannot be blank")
            title.length > 255 -> ValidationResult.Invalid("Book title must not exceed 255 characters")
            else -> ValidationResult.Valid
        }
    }

    fun validateAuthor(author: String): ValidationResult {
        return when {
            author.isBlank() -> ValidationResult.Invalid("Book author cannot be blank")
            author.length > 255 -> ValidationResult.Invalid("Book author must not exceed 255 characters")
            else -> ValidationResult.Valid
        }
    }

    fun validatePublisher(publisher: String): ValidationResult {
        return when {
            publisher.isBlank() -> ValidationResult.Invalid("Book publisher cannot be blank")
            publisher.length > 255 -> ValidationResult.Invalid("Book publisher must not exceed 255 characters")
            else -> ValidationResult.Valid
        }
    }

    fun validatePublishingYear(year: Int): ValidationResult {
        return when {
            year < MIN_PUBLISHING_YEAR || year > MAX_PUBLISHING_YEAR ->
                ValidationResult.Invalid(
                    "Invalid publishing year. Must be between $MIN_PUBLISHING_YEAR and $MAX_PUBLISHING_YEAR",
                )
            else -> ValidationResult.Valid
        }
    }

    fun validateCategoryName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Invalid("Category name cannot be blank")
            name.length > 255 -> ValidationResult.Invalid("Category name must not exceed 255 characters")
            else -> ValidationResult.Valid
        }
    }

    fun validateCategoryDescription(description: String): ValidationResult {
        return when {
            description.isBlank() -> ValidationResult.Invalid("Category description cannot be blank")
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
