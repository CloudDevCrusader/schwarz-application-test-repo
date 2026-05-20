package com.schwarzdigitale.domain.repositories

import com.schwarzdigitale.domain.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class CategoryRepository {
    
    fun create(request: CategoryCreateRequest): Category = transaction {
        val id = Categories.insertAndGetId {
            it[name] = request.name
            it[description] = request.description
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        
        Category(
            id = id.value,
            name = request.name,
            description = request.description,
            bookCount = 0
        )
    }
    
    fun findAll(): List<Category> = transaction {
        Categories.selectAll().map { row ->
            val categoryId = row[Categories.id].value
            val bookCount = Books.selectAll().where { Books.categoryId eq categoryId }.count().toInt()
            
            Category(
                id = categoryId,
                name = row[Categories.name],
                description = row[Categories.description],
                bookCount = bookCount
            )
        }
    }
    
    fun findById(id: Int): Category? = transaction {
        Categories.selectAll().where { Categories.id eq id }
            .mapNotNull { row ->
                val bookCount = Books.selectAll().where { Books.categoryId eq id }.count().toInt()
                
                Category(
                    id = row[Categories.id].value,
                    name = row[Categories.name],
                    description = row[Categories.description],
                    bookCount = bookCount
                )
            }
            .singleOrNull()
    }
    
    fun update(id: Int, request: CategoryUpdateRequest): Boolean = transaction {
        val updateCount = Categories.update({ Categories.id eq id }) {
            request.name?.let { name -> it[Categories.name] = name }
            request.description?.let { desc -> it[description] = desc }
            it[updatedAt] = LocalDateTime.now()
        }
        updateCount > 0
    }
    
    fun delete(id: Int): Boolean = transaction {
        // Check if category has books
        val bookCount = Books.selectAll().where { Books.categoryId eq id }.count()
        if (bookCount > 0) {
            throw IllegalStateException("Cannot delete category with existing books")
        }
        
        val deleteCount = Categories.deleteWhere { Categories.id eq id }
        deleteCount > 0
    }
}
