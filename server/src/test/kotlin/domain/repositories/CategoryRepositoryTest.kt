package com.schwarzdigitale.domain.repositories

import com.schwarzdigitale.domain.models.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

class CategoryRepositoryTest : DescribeSpec({
    
    beforeSpec {
        Database.connect("jdbc:h2:mem:test_category;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(Categories, Books)
        }
    }
    
    afterSpec {
        transaction {
            SchemaUtils.drop(Books, Categories)
        }
    }
    
    afterTest {
        transaction {
            Books.deleteAll()
            Categories.deleteAll()
        }
    }
    
    describe("CategoryRepository.create") {
        
        it("should create a new category") {
            val repository = CategoryRepository()
            val request = CategoryCreateRequest(
                name = "Science Fiction",
                description = "Sci-fi books"
            )
            
            val result = repository.create(request)
            
            result.id shouldNotBe null
            result.name shouldBe "Science Fiction"
            result.description shouldBe "Sci-fi books"
            result.bookCount shouldBe 0
        }
    }
    
    describe("CategoryRepository.findById") {
        
        it("should find category by id") {
            val repository = CategoryRepository()
            val created = repository.create(
                CategoryCreateRequest("Fantasy", "Fantasy novels")
            )
            
            val found = repository.findById(created.id!!)
            
            found shouldNotBe null
            found!!.id shouldBe created.id
            found.name shouldBe "Fantasy"
            found.description shouldBe "Fantasy novels"
        }
        
        it("should return null for non-existent id") {
            val repository = CategoryRepository()
            
            val found = repository.findById(99999)
            
            found shouldBe null
        }
        
        it("should include book count") {
            val repository = CategoryRepository()
            val bookRepository = BookRepository()
            
            val category = repository.create(
                CategoryCreateRequest("Mystery", "Mystery books")
            )
            
            // Add books to the category
            bookRepository.create(
                BookCreateRequest(
                    title = "Book 1",
                    author = "Author 1",
                    publisher = "Publisher",
                    publishingYear = 2020,
                    categoryId = category.id!!
                )
            )
            
            bookRepository.create(
                BookCreateRequest(
                    title = "Book 2",
                    author = "Author 2",
                    publisher = "Publisher",
                    publishingYear = 2021,
                    categoryId = category.id
                )
            )
            
            val found = repository.findById(category.id)
            
            found!!.bookCount shouldBe 2
        }
    }
    
    describe("CategoryRepository.findAll") {
        
        it("should return all categories") {
            val repository = CategoryRepository()
            repository.create(CategoryCreateRequest("Cat 1", "Description 1"))
            repository.create(CategoryCreateRequest("Cat 2", "Description 2"))
            repository.create(CategoryCreateRequest("Cat 3", "Description 3"))
            
            val all = repository.findAll()
            
            all shouldHaveSize 3
        }
        
        it("should include book counts for all categories") {
            val repository = CategoryRepository()
            val bookRepository = BookRepository()
            
            val cat1 = repository.create(CategoryCreateRequest("Category 1", "Desc 1"))
            val cat2 = repository.create(CategoryCreateRequest("Category 2", "Desc 2"))
            
            // Add book to cat1
            bookRepository.create(
                BookCreateRequest(
                    title = "Test Book",
                    author = "Author",
                    publisher = "Publisher",
                    publishingYear = 2020,
                    categoryId = cat1.id!!
                )
            )
            
            val all = repository.findAll()
            
            val category1 = all.find { it.id == cat1.id }
            val category2 = all.find { it.id == cat2.id }
            
            category1!!.bookCount shouldBe 1
            category2!!.bookCount shouldBe 0
        }
    }
    
    describe("CategoryRepository.update") {
        
        it("should update category name") {
            val repository = CategoryRepository()
            val created = repository.create(
                CategoryCreateRequest("Old Name", "Description")
            )
            
            val updated = repository.update(
                created.id!!,
                CategoryUpdateRequest(name = "New Name")
            )
            
            updated shouldBe true
            val found = repository.findById(created.id)
            found!!.name shouldBe "New Name"
        }
        
        it("should update category description") {
            val repository = CategoryRepository()
            val created = repository.create(
                CategoryCreateRequest("Name", "Old Description")
            )
            
            repository.update(
                created.id!!,
                CategoryUpdateRequest(description = "New Description")
            )
            
            val found = repository.findById(created.id)
            found!!.description shouldBe "New Description"
        }
        
        it("should return false for non-existent category") {
            val repository = CategoryRepository()
            
            val updated = repository.update(
                99999,
                CategoryUpdateRequest(name = "New Name")
            )
            
            updated shouldBe false
        }
    }
    
    describe("CategoryRepository.delete") {
        
        it("should delete category with no books") {
            val repository = CategoryRepository()
            val created = repository.create(
                CategoryCreateRequest("To Delete", "Description")
            )
            
            val deleted = repository.delete(created.id!!)
            
            deleted shouldBe true
            repository.findById(created.id) shouldBe null
        }
        
        it("should throw exception when deleting category with books") {
            val repository = CategoryRepository()
            val bookRepository = BookRepository()
            
            val category = repository.create(
                CategoryCreateRequest("With Books", "Description")
            )
            
            bookRepository.create(
                BookCreateRequest(
                    title = "Book",
                    author = "Author",
                    publisher = "Publisher",
                    publishingYear = 2020,
                    categoryId = category.id!!
                )
            )
            
            shouldThrow<IllegalStateException> {
                repository.delete(category.id)
            }
        }
        
        it("should return false for non-existent category") {
            val repository = CategoryRepository()
            
            val deleted = repository.delete(99999)
            
            deleted shouldBe false
        }
    }
})
