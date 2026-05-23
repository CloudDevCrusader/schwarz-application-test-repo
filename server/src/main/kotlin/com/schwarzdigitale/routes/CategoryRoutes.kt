package com.schwarzdigital.routes

import com.schwarzdigital.auth.ErrorResponse
import com.schwarzdigital.domain.models.CategoryCreateRequest
import com.schwarzdigital.domain.models.CategoryUpdateRequest
import com.schwarzdigital.domain.repositories.CategoryRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.categoryRoutes(categoryRepository: CategoryRepository) {
    // Public read access - anonymous users can view categories
    route("/categories") {
        // Get all categories (public)
        get {
            try {
                val categories = categoryRepository.findAll()
                call.respond(HttpStatusCode.OK, categories)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", e.message ?: "Failed to retrieve categories"),
                )
            }
        }

        // Get category by ID (public)
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Invalid category ID"),
                    )
                    return@get
                }

                val category = categoryRepository.findById(id)
                if (category == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Category not found"),
                    )
                    return@get
                }

                call.respond(HttpStatusCode.OK, category)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", e.message ?: "Failed to retrieve category"),
                )
            }
        }

        // Authenticated routes - only authenticated users can create, update, delete
        authenticate("auth-jwt") {
            // Create category (authenticated only)
            post {
                try {
                    val request = call.receive<CategoryCreateRequest>()

                    // Validate request
                    if (request.name.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("VALIDATION_ERROR", "Category name cannot be blank"),
                        )
                        return@post
                    }

                    if (request.description.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("VALIDATION_ERROR", "Category description cannot be blank"),
                        )
                        return@post
                    }

                    val category = categoryRepository.create(request)
                    call.respond(HttpStatusCode.Created, category)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", e.message ?: "Failed to create category"),
                    )
                }
            }

            // Update category (authenticated only)
            put("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_ID", "Invalid category ID"),
                        )
                        return@put
                    }

                    val request = call.receive<CategoryUpdateRequest>()

                    // Validate request
                    request.name?.let { name ->
                        if (name.isBlank()) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("VALIDATION_ERROR", "Category name cannot be blank"),
                            )
                            return@put
                        }
                    }

                    request.description?.let { description ->
                        if (description.isBlank()) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("VALIDATION_ERROR", "Category description cannot be blank"),
                            )
                            return@put
                        }
                    }

                    val updated = categoryRepository.update(id, request)
                    if (!updated) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "Category not found"),
                        )
                        return@put
                    }

                    call.respond(HttpStatusCode.OK, mapOf("message" to "Category updated successfully"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", e.message ?: "Failed to update category"),
                    )
                }
            }

            // Delete category (authenticated only)
            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_ID", "Invalid category ID"),
                        )
                        return@delete
                    }

                    val deleted = categoryRepository.delete(id)
                    if (!deleted) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "Category not found"),
                        )
                        return@delete
                    }

                    call.respond(HttpStatusCode.OK, mapOf("message" to "Category deleted successfully"))
                } catch (e: IllegalStateException) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        ErrorResponse("CONFLICT", e.message ?: "Cannot delete category"),
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", e.message ?: "Failed to delete category"),
                    )
                }
            }
        }
    }
}
