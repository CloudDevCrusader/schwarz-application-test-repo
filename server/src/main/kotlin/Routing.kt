package com.schwarzdigital

import com.schwarzdigital.auth.JwtService
import com.schwarzdigital.domain.repositories.BookRepository
import com.schwarzdigital.domain.repositories.CategoryRepository
import com.schwarzdigital.domain.repositories.CustomerRepository
import com.schwarzdigital.routes.authRoutes
import com.schwarzdigital.routes.bookRoutes
import com.schwarzdigital.routes.categoryRoutes
import com.schwarzdigital.routes.customerRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    // Initialize services and repositories
    val jwtService = JwtService(environment)
    val customerRepository = CustomerRepository()
    val categoryRepository = CategoryRepository()
    val bookRepository = BookRepository()

    routing {
        get("/") {
            call.respondText("Library API - Welcome! Use /auth/login or /auth/register to get started")
        }

        get("/health") {
            call.respond(mapOf("status" to "healthy", "service" to "library-api"))
        }

        // Authentication routes (login, register)
        authRoutes(jwtService, customerRepository)

        // Customer routes (all authenticated)
        customerRoutes(customerRepository)

        // Category routes (read public, write authenticated)
        categoryRoutes(categoryRepository)

        // Book routes (read public, write authenticated)
        bookRoutes(bookRepository)
    }
}
