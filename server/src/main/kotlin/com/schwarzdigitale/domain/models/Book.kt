package com.schwarzdigitale.domain.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

// Database Table Definition
object Books : IntIdTable("books") {
    val title = varchar("title", 255)
    val author = varchar("author", 255)
    val publisher = varchar("publisher", 255)
    val publishingYear = integer("publishing_year")
    val categoryId = reference("category_id", Categories)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
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
    val categoryName: String? = null
)

// Request DTOs
@Serializable
data class BookCreateRequest(
    val title: String,
    val author: String,
    val publisher: String,
    val publishingYear: Int,
    val categoryId: Int
)

@Serializable
data class BookUpdateRequest(
    val title: String? = null,
    val author: String? = null,
    val publisher: String? = null,
    val publishingYear: Int? = null,
    val categoryId: Int? = null
)
