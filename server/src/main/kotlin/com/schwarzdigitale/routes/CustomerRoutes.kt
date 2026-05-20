package com.schwarzdigitale.routes

import com.schwarzdigitale.auth.ErrorResponse
import com.schwarzdigitale.auth.userId
import com.schwarzdigitale.domain.models.CustomerUpdateRequest
import com.schwarzdigitale.domain.repositories.CustomerRepository
import com.schwarzdigitale.utils.Validator
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.customerRoutes(customerRepository: CustomerRepository) {
    
    authenticate("auth-jwt") {
        
        // Get all customers (authenticated only)
        get("/customers") {
            try {
                val customers = customerRepository.findAll()
                call.respond(HttpStatusCode.OK, customers)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", e.message ?: "Failed to retrieve customers")
                )
            }
        }
        
        // Get customer by ID (authenticated only)
        get("/customers/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Invalid customer ID")
                    )
                    return@get
                }
                
                val customer = customerRepository.findById(id)
                if (customer == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Customer not found")
                    )
                    return@get
                }
                
                call.respond(HttpStatusCode.OK, customer)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", e.message ?: "Failed to retrieve customer")
                )
            }
        }
        
        // Update customer (authenticated only)
        put("/customers/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Invalid customer ID")
                    )
                    return@put
                }
                
                val request = call.receive<CustomerUpdateRequest>()
                
                // Validate fields if provided
                request.email?.let { email ->
                    val validation = Validator.validateEmail(email)
                    if (!validation.isValid()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("VALIDATION_ERROR", validation.getErrorMessage() ?: "Invalid email")
                        )
                        return@put
                    }
                }
                
                request.name?.let { name ->
                    val validation = Validator.validateName(name)
                    if (!validation.isValid()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("VALIDATION_ERROR", validation.getErrorMessage() ?: "Invalid name")
                        )
                        return@put
                    }
                }
                
                request.password?.let { password ->
                    val validation = Validator.validatePassword(password)
                    if (!validation.isValid()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("VALIDATION_ERROR", validation.getErrorMessage() ?: "Invalid password")
                        )
                        return@put
                    }
                }
                
                val updated = customerRepository.update(id, request)
                if (!updated) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Customer not found")
                    )
                    return@put
                }
                
                call.respond(HttpStatusCode.OK, mapOf("message" to "Customer updated successfully"))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", e.message ?: "Failed to update customer")
                )
            }
        }
        
        // Delete customer (authenticated only)
        delete("/customers/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Invalid customer ID")
                    )
                    return@delete
                }
                
                val deleted = customerRepository.delete(id)
                if (!deleted) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Customer not found")
                    )
                    return@delete
                }
                
                call.respond(HttpStatusCode.OK, mapOf("message" to "Customer deleted successfully"))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", e.message ?: "Failed to delete customer")
                )
            }
        }
    }
}
