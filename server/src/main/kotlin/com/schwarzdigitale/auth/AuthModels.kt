package com.schwarzdigital.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val token: String,
    val customer: CustomerInfo,
)

@Serializable
data class CustomerInfo(
    val id: Int,
    val name: String,
    val email: String,
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
)
