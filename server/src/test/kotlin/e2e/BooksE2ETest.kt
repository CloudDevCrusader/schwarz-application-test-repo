package com.schwarzdigital.e2e

import com.schwarzdigital.auth.ErrorResponse
import com.schwarzdigital.auth.LoginRequest
import com.schwarzdigital.auth.LoginResponse
import com.schwarzdigital.auth.configureAuth
import com.schwarzdigital.configureDatabase
import com.schwarzdigital.configureRouting
import com.schwarzdigital.configureSerialization
import com.schwarzdigital.domain.models.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class BooksE2ETest {
    private val testEmail = "books-e2e@test.com"
    private val testPassword = "password123"
    private val testName = "Books E2E Test User"

    @Test
    fun `test complete books CRUD flow`() =
        testApplication {
            application {
                configureSerialization()
                configureDatabase()
                configureAuth()
                configureRouting()
            }
            val client =
                createClient {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }

            // Step 1: Register and login
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(CustomerCreateRequest(testName, testEmail, testPassword))
            }

            val loginResponse =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(testEmail, testPassword))
                }.body<LoginResponse>()

            val token = loginResponse.token

            // Step 2: Create a category for the book
            val category =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(CategoryCreateRequest("E2E Fiction", "Fiction category for E2E testing"))
                }.body<Category>()

            // Step 3: Create a book
            val createResponse =
                client.post("/books") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(
                        BookCreateRequest(
                            title = "Test Book",
                            author = "Test Author",
                            publisher = "Test Publisher",
                            publishingYear = 2024,
                            categoryId = category.id!!,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.Created, createResponse.status)
            val createdBook = createResponse.body<Book>()
            assertNotNull(createdBook.id)
            assertEquals("Test Book", createdBook.title)
            assertEquals("Test Author", createdBook.author)
            assertEquals("E2E Fiction", createdBook.categoryName)

            // Step 4: Read the book (public endpoint)
            val getResponse = client.get("/books/${createdBook.id}")
            assertEquals(HttpStatusCode.OK, getResponse.status)
            val fetchedBook = getResponse.body<Book>()
            assertEquals(createdBook.id, fetchedBook.id)
            assertEquals("Test Book", fetchedBook.title)

            // Step 5: Update the book
            val updateResponse =
                client.put("/books/${createdBook.id}") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(
                        BookUpdateRequest(
                            title = "Updated Book Title",
                            publishingYear = 2025,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.OK, updateResponse.status)

            // Step 6: Verify update
            val updatedBook = client.get("/books/${createdBook.id}").body<Book>()
            assertEquals("Updated Book Title", updatedBook.title)
            assertEquals(2025, updatedBook.publishingYear)
            assertEquals("Test Author", updatedBook.author) // Unchanged

            // Step 7: Get all books
            val allBooksResponse = client.get("/books")
            assertEquals(HttpStatusCode.OK, allBooksResponse.status)
            val allBooks = allBooksResponse.body<List<Book>>()
            assertTrue(allBooks.any { it.id == createdBook.id })

            // Step 8: Delete the book
            val deleteResponse =
                client.delete("/books/${createdBook.id}") {
                    bearerAuth(token)
                }

            assertEquals(HttpStatusCode.OK, deleteResponse.status)

            // Step 9: Verify deletion
            val notFoundResponse = client.get("/books/${createdBook.id}")
            assertEquals(HttpStatusCode.NotFound, notFoundResponse.status)
        }

    @Test
    fun `test create book with invalid data`() =
        testApplication {
            application {
                configureSerialization()
                configureDatabase()
                configureAuth()
                configureRouting()
            }
            val client =
                createClient {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }

            // Register and login
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(CustomerCreateRequest(testName, "invalid-book@test.com", testPassword))
            }

            val token =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest("invalid-book@test.com", testPassword))
                }.body<LoginResponse>().token

            // Create category
            val category =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(CategoryCreateRequest("Test Invalid Data", "Test category for invalid data test"))
                }.body<Category>()

            // Test blank title
            val blankTitleResponse =
                client.post("/books") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(
                        BookCreateRequest(
                            title = "   ",
                            author = "Author",
                            publisher = "Publisher",
                            publishingYear = 2020,
                            categoryId = category.id!!,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.BadRequest, blankTitleResponse.status)
            val error = blankTitleResponse.body<ErrorResponse>()
            assertEquals("VALIDATION_ERROR", error.error)

            // Test invalid year
            val invalidYearResponse =
                client.post("/books") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(
                        BookCreateRequest(
                            title = "Book",
                            author = "Author",
                            publisher = "Publisher",
                            publishingYear = 999,
                            categoryId = category.id!!,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.BadRequest, invalidYearResponse.status)

            // Test non-existent category
            val invalidCategoryResponse =
                client.post("/books") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(
                        BookCreateRequest(
                            title = "Book",
                            author = "Author",
                            publisher = "Publisher",
                            publishingYear = 2020,
                            categoryId = 99999,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.BadRequest, invalidCategoryResponse.status)
        }

    @Test
    fun `test book operations without authentication`() =
        testApplication {
            application {
                configureSerialization()
                configureDatabase()
                configureAuth()
                configureRouting()
            }
            val client =
                createClient {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }

            // Create book without auth should fail
            val createResponse =
                client.post("/books") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        BookCreateRequest(
                            title = "Book",
                            author = "Author",
                            publisher = "Publisher",
                            publishingYear = 2020,
                            categoryId = 1,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.Unauthorized, createResponse.status)

            // Reading books should work (public)
            val getResponse = client.get("/books")
            assertEquals(HttpStatusCode.OK, getResponse.status)
        }

    @Test
    fun `test get books by category`() =
        testApplication {
            application {
                configureSerialization()
                configureDatabase()
                configureAuth()
                configureRouting()
            }
            val client =
                createClient {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }

            // Register and login
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(CustomerCreateRequest(testName, "category-books@test.com", testPassword))
            }

            val token =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest("category-books@test.com", testPassword))
                }.body<LoginResponse>().token

            // Create two categories
            val category1 =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(CategoryCreateRequest("Category 1", "First"))
                }.body<Category>()

            val category2 =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(CategoryCreateRequest("Category 2", "Second"))
                }.body<Category>()

            // Create books in different categories
            client.post("/books") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(
                    BookCreateRequest(
                        "Book in Cat 1",
                        "Author",
                        "Publisher",
                        2020,
                        category1.id!!,
                    ),
                )
            }

            client.post("/books") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(
                    BookCreateRequest(
                        "Another in Cat 1",
                        "Author",
                        "Publisher",
                        2021,
                        category1.id!!,
                    ),
                )
            }

            client.post("/books") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(
                    BookCreateRequest(
                        "Book in Cat 2",
                        "Author",
                        "Publisher",
                        2020,
                        category2.id!!,
                    ),
                )
            }

            // Get books by category 1
            val cat1Books = client.get("/books/category/${category1.id}").body<List<Book>>()
            assertEquals(2, cat1Books.size)
            assertTrue(cat1Books.all { it.categoryId == category1.id })

            // Get books by category 2
            val cat2Books = client.get("/books/category/${category2.id}").body<List<Book>>()
            assertEquals(1, cat2Books.size)
            assertEquals(category2.id, cat2Books[0].categoryId)
        }

    @Test
    fun `test update book with invalid year`() =
        testApplication {
            application {
                configureSerialization()
                configureDatabase()
                configureAuth()
                configureRouting()
            }
            val client =
                createClient {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }

            // Setup
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(CustomerCreateRequest(testName, "update-year@test.com", testPassword))
            }

            val token =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest("update-year@test.com", testPassword))
                }.body<LoginResponse>().token

            val category =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(CategoryCreateRequest("Test", "Test"))
                }.body<Category>()

            val book =
                client.post("/books") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(BookCreateRequest("Book", "Author", "Publisher", 2020, category.id!!))
                }.body<Book>()

            // Try to update with invalid year
            val response =
                client.put("/books/${book.id}") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(BookUpdateRequest(publishingYear = 3000))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            val error = response.body<ErrorResponse>()
            assertTrue(error.message.contains("publishing year", ignoreCase = true))
        }
}
