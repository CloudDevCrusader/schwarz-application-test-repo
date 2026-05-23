package com.schwarzdigital.domain.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

// Database Table Definition
object Customers : IntIdTable("customers") {
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }
}

// Domain Model
@Serializable
data class Customer(
    val id: Int? = null,
    val name: String,
    val email: String,
    @kotlinx.serialization.Transient
    val passwordHash: String? = null,
)

// Request DTOs
@Serializable
data class CustomerCreateRequest(
    val name: String,
    val email: String,
    val password: String,
)

@Serializable
data class CustomerUpdateRequest(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
)

// Response DTO (without password)
@Serializable
data class CustomerResponse(
    val id: Int,
    val name: String,
    val email: String,
)
