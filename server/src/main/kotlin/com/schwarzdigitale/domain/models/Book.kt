package com.schwarzdigital.domain.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

// Database Table Definition
object Books : IntIdTable("books") {
    val title = varchar("title", 255)
    val author = varchar("author", 255)
    val publisher = varchar("publisher", 255)
    val publishingYear = integer("publishing_year")
    val categoryId = reference("category_id", Categories)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }
}

// Domain Model
@Serializable
data class Book(
    val id: Int? = null,
    val title: String,
    val author: String,
    val publisher: String,
    val publishingYear: Int,
    val categoryId: Int,
    val categoryName: String? = null,
)

// Request DTOs
@Serializable
data class BookCreateRequest(
    val title: String,
    val author: String,
    val publisher: String,
    val publishingYear: Int,
    val categoryId: Int,
)

@Serializable
data class BookUpdateRequest(
    val title: String? = null,
    val author: String? = null,
    val publisher: String? = null,
    val publishingYear: Int? = null,
    val categoryId: Int? = null,
)
