package com.schwarzdigital.domain.repositories

import com.schwarzdigital.domain.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class BookRepository {
    fun create(request: BookCreateRequest): Book =
        transaction {
            // Verify category exists
            val categoryExists = Categories.selectAll().where { Categories.id eq request.categoryId }.count() > 0
            if (!categoryExists) {
                throw IllegalArgumentException("Category with id ${request.categoryId} does not exist")
            }

            val id =
                Books.insertAndGetId {
                    it[title] = request.title
                    it[author] = request.author
                    it[publisher] = request.publisher
                    it[publishingYear] = request.publishingYear
                    it[categoryId] = request.categoryId
                    it[createdAt] = Instant.now()
                    it[updatedAt] = Instant.now()
                }

            // Fetch category name
            val categoryName =
                Categories.selectAll()
                    .where { Categories.id eq request.categoryId }
                    .map { it[Categories.name] }
                    .firstOrNull()

            Book(
                id = id.value,
                title = request.title,
                author = request.author,
                publisher = request.publisher,
                publishingYear = request.publishingYear,
                categoryId = request.categoryId,
                categoryName = categoryName,
            )
        }

    fun findAll(): List<Book> =
        transaction {
            (Books innerJoin Categories)
                .selectAll()
                .map { rowToBook(it) }
        }

    fun findById(id: Int): Book? =
        transaction {
            (Books innerJoin Categories)
                .selectAll()
                .where { Books.id eq id }
                .mapNotNull { rowToBook(it) }
                .singleOrNull()
        }

    fun findByCategory(categoryId: Int): List<Book> =
        transaction {
            (Books innerJoin Categories)
                .selectAll()
                .where { Books.categoryId eq categoryId }
                .map { rowToBook(it) }
        }

    fun update(
        id: Int,
        request: BookUpdateRequest,
    ): Boolean =
        transaction {
            // If updating category, verify it exists
            request.categoryId?.let { newCategoryId ->
                val categoryExists = Categories.selectAll().where { Categories.id eq newCategoryId }.count() > 0
                if (!categoryExists) {
                    throw IllegalArgumentException("Category with id $newCategoryId does not exist")
                }
            }

            val updateCount =
                Books.update({ Books.id eq id }) {
                    request.title?.let { title -> it[Books.title] = title }
                    request.author?.let { author -> it[Books.author] = author }
                    request.publisher?.let { publisher -> it[Books.publisher] = publisher }
                    request.publishingYear?.let { year -> it[publishingYear] = year }
                    request.categoryId?.let { catId -> it[categoryId] = catId }
                    it[updatedAt] = Instant.now()
                }
            updateCount > 0
        }

    fun delete(id: Int): Boolean =
        transaction {
            val deleteCount = Books.deleteWhere { Books.id eq id }
            deleteCount > 0
        }

    private fun rowToBook(row: ResultRow): Book =
        Book(
            id = row[Books.id].value,
            title = row[Books.title],
            author = row[Books.author],
            publisher = row[Books.publisher],
            publishingYear = row[Books.publishingYear],
            categoryId = row[Books.categoryId].value,
            categoryName = row[Categories.name],
        )
}
