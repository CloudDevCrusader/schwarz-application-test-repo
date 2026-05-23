package com.schwarzdigital

import com.schwarzdigital.auth.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("INTERNAL_ERROR", cause.message ?: "An unexpected error occurred"),
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("BAD_REQUEST", cause.message ?: "Invalid request"),
            )
        }

        exception<IllegalStateException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse("CONFLICT", cause.message ?: "Resource conflict"),
            )
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ErrorResponse("NOT_FOUND", "The requested resource was not found"),
            )
        }

        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respond(
                status,
                ErrorResponse("UNAUTHORIZED", "Authentication required"),
            )
        }
    }
}
