package com.schwarzdigitale

import com.schwarzdigitale.auth.JwtService
import com.schwarzdigitale.domain.repositories.BookRepository
import com.schwarzdigitale.domain.repositories.CategoryRepository
import com.schwarzdigitale.domain.repositories.CustomerRepository
import com.schwarzdigitale.routes.authRoutes
import com.schwarzdigitale.routes.bookRoutes
import com.schwarzdigitale.routes.categoryRoutes
import com.schwarzdigitale.routes.customerRoutes
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