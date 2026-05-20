package com.schwarzdigitale.domain.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

// Database Table Definition
object Customers : IntIdTable("customers") {
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

// Domain Model
@Serializable
data class Customer(
    val id: Int? = null,
    val name: String,
    val email: String,
    @Serializable(with = kotlinx.serialization.Transient::class)
    val passwordHash: String? = null
)

// Request DTOs
@Serializable
data class CustomerCreateRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class CustomerUpdateRequest(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null
)

// Response DTO (without password)
@Serializable
data class CustomerResponse(
    val id: Int,
    val name: String,
    val email: String
)
