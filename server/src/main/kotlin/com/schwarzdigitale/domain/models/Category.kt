package com.schwarzdigitale.domain.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

// Database Table Definition
object Categories : IntIdTable("categories") {
    val name = varchar("name", 255).uniqueIndex()
    val description = text("description")
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

// Domain Model
@Serializable
data class Category(
    val id: Int? = null,
    val name: String,
    val description: String,
    val bookCount: Int = 0
)

// Request DTOs
@Serializable
data class CategoryCreateRequest(
    val name: String,
    val description: String
)

@Serializable
data class CategoryUpdateRequest(
    val name: String? = null,
    val description: String? = null
)
