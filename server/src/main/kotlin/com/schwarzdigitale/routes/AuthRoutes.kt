package com.schwarzdigital.routes

import com.schwarzdigital.auth.*
import com.schwarzdigital.domain.models.CustomerCreateRequest
import com.schwarzdigital.domain.repositories.CustomerRepository
import com.schwarzdigital.utils.Validator
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(
    jwtService: JwtService,
    customerRepository: CustomerRepository,
) {
    post("/auth/register") {
        try {
            val request = call.receive<CustomerCreateRequest>()

            // Validate email
            val emailValidation = Validator.validateEmail(request.email)
            if (!emailValidation.isValid()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("VALIDATION_ERROR", emailValidation.getErrorMessage() ?: "Invalid email"),
                )
                return@post
            }

            // Validate password
            val passwordValidation = Validator.validatePassword(request.password)
            if (!passwordValidation.isValid()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("VALIDATION_ERROR", passwordValidation.getErrorMessage() ?: "Invalid password"),
                )
                return@post
            }

            // Validate name
            val nameValidation = Validator.validateName(request.name)
            if (!nameValidation.isValid()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("VALIDATION_ERROR", nameValidation.getErrorMessage() ?: "Invalid name"),
                )
                return@post
            }

            // Check if email already exists
            val existingCustomer = customerRepository.findByEmail(request.email)
            if (existingCustomer != null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse("EMAIL_EXISTS", "Email already registered"),
                )
                return@post
            }

            val customerResponse = customerRepository.create(request)
            call.respond(HttpStatusCode.Created, customerResponse)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("SERVER_ERROR", e.message ?: "An error occurred during registration"),
            )
        }
    }

    post("/auth/login") {
        try {
            val request = call.receive<LoginRequest>()

            // Validate email format
            if (!Validator.isValidEmail(request.email)) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("VALIDATION_ERROR", "Invalid email format"),
                )
                return@post
            }

            // Verify credentials
            val isValid = customerRepository.verifyPassword(request.email, request.password)
            if (!isValid) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("INVALID_CREDENTIALS", "Invalid email or password"),
                )
                return@post
            }

            // Get customer details
            val customer = customerRepository.findByEmail(request.email)
            if (customer == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("INVALID_CREDENTIALS", "Invalid email or password"),
                )
                return@post
            }

            // Generate JWT token
            val token = jwtService.generateToken(customer)

            val response =
                LoginResponse(
                    token = token,
                    customer =
                        CustomerInfo(
                            id = customer.id!!,
                            name = customer.name,
                            email = customer.email,
                        ),
                )

            call.respond(HttpStatusCode.OK, response)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("SERVER_ERROR", e.message ?: "An error occurred during login"),
            )
        }
    }
}
