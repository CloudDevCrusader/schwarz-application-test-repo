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

class BookRepositoryTest : DescribeSpec({
    
    lateinit var categoryRepository: CategoryRepository
    lateinit var bookRepository: BookRepository
    lateinit var testCategory: Category
    
    beforeSpec {
        Database.connect("jdbc:h2:mem:test_book;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(Categories, Books)
        }
    }
    
    afterSpec {
        transaction {
            SchemaUtils.drop(Books, Categories)
        }
    }
    
    beforeTest {
        categoryRepository = CategoryRepository()
        bookRepository = BookRepository()
        
        testCategory = categoryRepository.create(
            CategoryCreateRequest("Test Category", "For testing books")
        )
    }
    
    afterTest {
        transaction {
            Books.deleteAll()
            Categories.deleteAll()
        }
    }
    
    describe("BookRepository.create") {
        
        it("should create a new book") {
            val request = BookCreateRequest(
                title = "The Great Gatsby",
                author = "F. Scott Fitzgerald",
                publisher = "Scribner",
                publishingYear = 1925,
                categoryId = testCategory.id!!
            )
            
            val result = bookRepository.create(request)
            
            result.id shouldNotBe null
            result.title shouldBe "The Great Gatsby"
            result.author shouldBe "F. Scott Fitzgerald"
            result.publisher shouldBe "Scribner"
            result.publishingYear shouldBe 1925
            result.categoryId shouldBe testCategory.id
            result.categoryName shouldBe "Test Category"
        }
        
        it("should throw exception when category does not exist") {
            val request = BookCreateRequest(
                title = "Test Book",
                author = "Author",
                publisher = "Publisher",
                publishingYear = 2020,
                categoryId = 99999
            )
            
            shouldThrow<IllegalArgumentException> {
                bookRepository.create(request)
            }
        }
    }
    
    describe("BookRepository.findById") {
        
        it("should find book by id") {
            val created = bookRepository.create(
                BookCreateRequest(
                    title = "1984",
                    author = "George Orwell",
                    publisher = "Secker & Warburg",
                    publishingYear = 1949,
                    categoryId = testCategory.id!!
                )
            )
            
            val found = bookRepository.findById(created.id!!)
            
            found shouldNotBe null
            found!!.id shouldBe created.id
            found.title shouldBe "1984"
            found.author shouldBe "George Orwell"
            found.categoryName shouldBe "Test Category"
        }
        
        it("should return null for non-existent id") {
            val found = bookRepository.findById(99999)
            
            found shouldBe null
        }
    }
    
    describe("BookRepository.findAll") {
        
        it("should return all books") {
            bookRepository.create(
                BookCreateRequest("Book 1", "Author 1", "Publisher", 2020, testCategory.id!!)
            )
            bookRepository.create(
                BookCreateRequest("Book 2", "Author 2", "Publisher", 2021, testCategory.id!!)
            )
            bookRepository.create(
                BookCreateRequest("Book 3", "Author 3", "Publisher", 2022, testCategory.id!!)
            )
            
            val all = bookRepository.findAll()
            
            all shouldHaveSize 3
        }
        
        it("should include category name for all books") {
            bookRepository.create(
                BookCreateRequest("Test Book", "Author", "Publisher", 2020, testCategory.id!!)
            )
            
            val all = bookRepository.findAll()
            
            all.first().categoryName shouldBe "Test Category"
        }
    }
    
    describe("BookRepository.findByCategory") {
        
        it("should find books by category") {
            val category2 = categoryRepository.create(
                CategoryCreateRequest("Category 2", "Second category")
            )
            
            bookRepository.create(
                BookCreateRequest("Book in Cat 1", "Author", "Publisher", 2020, testCategory.id!!)
            )
            bookRepository.create(
                BookCreateRequest("Book in Cat 1 (2)", "Author", "Publisher", 2021, testCategory.id!!)
            )
            bookRepository.create(
                BookCreateRequest("Book in Cat 2", "Author", "Publisher", 2020, category2.id!!)
            )
            
            val booksInCategory1 = bookRepository.findByCategory(testCategory.id!!)
            val booksInCategory2 = bookRepository.findByCategory(category2.id!!)
            
            booksInCategory1 shouldHaveSize 2
            booksInCategory2 shouldHaveSize 1
        }
        
        it("should return empty list for category with no books") {
            val emptyCategory = categoryRepository.create(
                CategoryCreateRequest("Empty", "No books")
            )
            
            val books = bookRepository.findByCategory(emptyCategory.id!!)
            
            books shouldHaveSize 0
        }
    }
    
    describe("BookRepository.update") {
        
        it("should update book title") {
            val created = bookRepository.create(
                BookCreateRequest("Old Title", "Author", "Publisher", 2020, testCategory.id!!)
            )
            
            val updated = bookRepository.update(
                created.id!!,
                BookUpdateRequest(title = "New Title")
            )
            
            updated shouldBe true
            val found = bookRepository.findById(created.id)
            found!!.title shouldBe "New Title"
        }
        
        it("should update book author") {
            val created = bookRepository.create(
                BookCreateRequest("Title", "Old Author", "Publisher", 2020, testCategory.id!!)
            )
            
            bookRepository.update(
                created.id!!,
                BookUpdateRequest(author = "New Author")
            )
            
            val found = bookRepository.findById(created.id)
            found!!.author shouldBe "New Author"
        }
        
        it("should update book category") {
            val newCategory = categoryRepository.create(
                CategoryCreateRequest("New Category", "Description")
            )
            
            val created = bookRepository.create(
                BookCreateRequest("Title", "Author", "Publisher", 2020, testCategory.id!!)
            )
            
            bookRepository.update(
                created.id!!,
                BookUpdateRequest(categoryId = newCategory.id)
            )
            
            val found = bookRepository.findById(created.id)
            found!!.categoryId shouldBe newCategory.id
            found.categoryName shouldBe "New Category"
        }
        
        it("should throw exception when updating to non-existent category") {
            val created = bookRepository.create(
                BookCreateRequest("Title", "Author", "Publisher", 2020, testCategory.id!!)
            )
            
            shouldThrow<IllegalArgumentException> {
                bookRepository.update(
                    created.id!!,
                    BookUpdateRequest(categoryId = 99999)
                )
            }
        }
        
        it("should update publishing year") {
            val created = bookRepository.create(
                BookCreateRequest("Title", "Author", "Publisher", 2020, testCategory.id!!)
            )
            
            bookRepository.update(
                created.id!!,
                BookUpdateRequest(publishingYear = 2024)
            )
            
            val found = bookRepository.findById(created.id)
            found!!.publishingYear shouldBe 2024
        }
        
        it("should return false for non-existent book") {
            val updated = bookRepository.update(
                99999,
                BookUpdateRequest(title = "New Title")
            )
            
            updated shouldBe false
        }
    }
    
    describe("BookRepository.delete") {
        
        it("should delete book") {
            val created = bookRepository.create(
                BookCreateRequest("To Delete", "Author", "Publisher", 2020, testCategory.id!!)
            )
            
            val deleted = bookRepository.delete(created.id!!)
            
            deleted shouldBe true
            bookRepository.findById(created.id) shouldBe null
        }
        
        it("should return false for non-existent book") {
            val deleted = bookRepository.delete(99999)
            
            deleted shouldBe false
        }
    }
})
