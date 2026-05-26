package com.schwarzdigital.routes

import com.schwarzdigital.auth.ErrorResponse
import com.schwarzdigital.domain.models.BookCreateRequest
import com.schwarzdigital.domain.models.BookUpdateRequest
import com.schwarzdigital.domain.repositories.BookRepository
import com.schwarzdigital.utils.Validator
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bookRoutes(bookRepository: BookRepository) {
    // Public read access - anonymous users can view books
    route("/books") {
        // Get all books (public)
        get {
            try {
                val books = bookRepository.findAll()
                call.respond(HttpStatusCode.OK, books)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", e.message ?: "Failed to retrieve books"),
                )
            }
        }

        // Get book by ID (public)
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Invalid book ID"),
                    )
                    return@get
                }

                val book = bookRepository.findById(id)
                if (book == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Book not found"),
                    )
                    return@get
                }

                call.respond(HttpStatusCode.OK, book)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", e.message ?: "Failed to retrieve book"),
                )
            }
        }

        // Get books by category (public)
        get("/category/{categoryId}") {
            try {
                val categoryId = call.parameters["categoryId"]?.toIntOrNull()
                if (categoryId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Invalid category ID"),
                    )
                    return@get
                }

                val books = bookRepository.findByCategory(categoryId)
                call.respond(HttpStatusCode.OK, books)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", e.message ?: "Failed to retrieve books"),
                )
            }
        }

        // Authenticated routes - only authenticated users can create, update, delete
        authenticate("auth-jwt") {
            // Create book (authenticated only)
            post {
                try {
                    val request = call.receive<BookCreateRequest>()

                    // Validate title
                    val titleValidation = Validator.validateBookTitle(request.title)
                    if (!titleValidation.isValid()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("VALIDATION_ERROR", titleValidation.getErrorMessage() ?: "Invalid title"),
                        )
                        return@post
                    }

                    // Validate author
                    val authorValidation = Validator.validateAuthor(request.author)
                    if (!authorValidation.isValid()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("VALIDATION_ERROR", authorValidation.getErrorMessage() ?: "Invalid author"),
                        )
                        return@post
                    }

                    // Validate publisher
                    val publisherValidation = Validator.validatePublisher(request.publisher)
                    if (!publisherValidation.isValid()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("VALIDATION_ERROR", publisherValidation.getErrorMessage() ?: "Invalid publisher"),
                        )
                        return@post
                    }

                    // Validate publishing year
                    val yearValidation = Validator.validatePublishingYear(request.publishingYear)
                    if (!yearValidation.isValid()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("VALIDATION_ERROR", yearValidation.getErrorMessage() ?: "Invalid publishing year"),
                        )
                        return@post
                    }

                    val book = bookRepository.create(request)
                    call.respond(HttpStatusCode.Created, book)
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("VALIDATION_ERROR", e.message ?: "Invalid book data"),
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", e.message ?: "Failed to create book"),
                    )
                }
            }

            // Update book (authenticated only)
            put("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_ID", "Invalid book ID"),
                        )
                        return@put
                    }

                    val request = call.receive<BookUpdateRequest>()

                    // Validate title if provided
                    request.title?.let { title ->
                        val validation = Validator.validateBookTitle(title)
                        if (!validation.isValid()) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("VALIDATION_ERROR", validation.getErrorMessage() ?: "Invalid title"),
                            )
                            return@put
                        }
                    }

                    // Validate author if provided
                    request.author?.let { author ->
                        val validation = Validator.validateAuthor(author)
                        if (!validation.isValid()) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("VALIDATION_ERROR", validation.getErrorMessage() ?: "Invalid author"),
                            )
                            return@put
                        }
                    }

                    // Validate publisher if provided
                    request.publisher?.let { publisher ->
                        val validation = Validator.validatePublisher(publisher)
                        if (!validation.isValid()) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("VALIDATION_ERROR", validation.getErrorMessage() ?: "Invalid publisher"),
                            )
                            return@put
                        }
                    }

                    // Validate publishing year if provided
                    request.publishingYear?.let { year ->
                        val validation = Validator.validatePublishingYear(year)
                        if (!validation.isValid()) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("VALIDATION_ERROR", validation.getErrorMessage() ?: "Invalid publishing year"),
                            )
                            return@put
                        }
                    }

                    val updated = bookRepository.update(id, request)
                    if (!updated) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "Book not found"),
                        )
                        return@put
                    }

                    call.respond(HttpStatusCode.OK, mapOf("message" to "Book updated successfully"))
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("VALIDATION_ERROR", e.message ?: "Invalid book data"),
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", e.message ?: "Failed to update book"),
                    )
                }
            }

            // Delete book (authenticated only)
            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_ID", "Invalid book ID"),
                        )
                        return@delete
                    }

                    val deleted = bookRepository.delete(id)
                    if (!deleted) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "Book not found"),
                        )
                        return@delete
                    }

                    call.respond(HttpStatusCode.OK, mapOf("message" to "Book deleted successfully"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", e.message ?: "Failed to delete book"),
                    )
                }
            }
        }
    }
}
