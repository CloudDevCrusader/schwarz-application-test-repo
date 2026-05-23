package com.schwarzdigital.domain.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

// Database Table Definition
object Categories : IntIdTable("categories") {
    val name = varchar("name", 255).uniqueIndex()
    val description = text("description")
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }
}

// Domain Model
@Serializable
data class Category(
    val id: Int? = null,
    val name: String,
    val description: String,
    val bookCount: Int = 0,
)

// Request DTOs
@Serializable
data class CategoryCreateRequest(
    val name: String,
    val description: String,
)

@Serializable
data class CategoryUpdateRequest(
    val name: String? = null,
    val description: String? = null,
)
