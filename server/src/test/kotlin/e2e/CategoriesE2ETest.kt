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

class CategoriesE2ETest {
    private val testEmail = "categories-e2e@test.com"
    private val testPassword = "password123"
    private val testName = "Categories E2E Test User"

    @Test
    fun `test complete categories CRUD flow`() =
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

            // Step 2: Create a category
            val createResponse =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(
                        CategoryCreateRequest(
                            name = "E2E Test Category",
                            description = "Category created for E2E testing",
                        ),
                    )
                }

            assertEquals(HttpStatusCode.Created, createResponse.status)
            val createdCategory = createResponse.body<Category>()
            assertNotNull(createdCategory.id)
            assertEquals("E2E Test Category", createdCategory.name)
            assertEquals("Category created for E2E testing", createdCategory.description)
            assertEquals(0, createdCategory.bookCount)

            // Step 3: Read the category (public endpoint)
            val getResponse = client.get("/categories/${createdCategory.id}")
            assertEquals(HttpStatusCode.OK, getResponse.status)
            val fetchedCategory = getResponse.body<Category>()
            assertEquals(createdCategory.id, fetchedCategory.id)
            assertEquals("E2E Test Category", fetchedCategory.name)

            // Step 4: Update the category
            val updateResponse =
                client.put("/categories/${createdCategory.id}") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(
                        CategoryUpdateRequest(
                            name = "Updated Category Name",
                            description = "Updated description",
                        ),
                    )
                }

            assertEquals(HttpStatusCode.OK, updateResponse.status)

            // Step 5: Verify update
            val updatedCategory = client.get("/categories/${createdCategory.id}").body<Category>()
            assertEquals("Updated Category Name", updatedCategory.name)
            assertEquals("Updated description", updatedCategory.description)

            // Step 6: Get all categories
            val allCategoriesResponse = client.get("/categories")
            assertEquals(HttpStatusCode.OK, allCategoriesResponse.status)
            val allCategories = allCategoriesResponse.body<List<Category>>()
            assertTrue(allCategories.any { it.id == createdCategory.id })

            // Step 7: Delete the category
            val deleteResponse =
                client.delete("/categories/${createdCategory.id}") {
                    bearerAuth(token)
                }

            assertEquals(HttpStatusCode.OK, deleteResponse.status)

            // Step 8: Verify deletion
            val notFoundResponse = client.get("/categories/${createdCategory.id}")
            assertEquals(HttpStatusCode.NotFound, notFoundResponse.status)
        }

    @Test
    fun `test category with books cannot be deleted`() =
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
                setBody(CustomerCreateRequest(testName, "delete-cat@test.com", testPassword))
            }

            val token =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest("delete-cat@test.com", testPassword))
                }.body<LoginResponse>().token

            // Create category
            val category =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(CategoryCreateRequest("Category with Books", "Has books"))
                }.body<Category>()

            // Add a book to the category
            client.post("/books") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(
                    BookCreateRequest(
                        "Book in Category",
                        "Author",
                        "Publisher",
                        2020,
                        category.id!!,
                    ),
                )
            }

            // Try to delete category with books
            val deleteResponse =
                client.delete("/categories/${category.id}") {
                    bearerAuth(token)
                }

            assertEquals(HttpStatusCode.Conflict, deleteResponse.status)
            val error = deleteResponse.body<ErrorResponse>()
            assertEquals("CONFLICT", error.error)
        }

    @Test
    fun `test category book count is accurate`() =
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
                setBody(CustomerCreateRequest(testName, "book-count@test.com", testPassword))
            }

            val token =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest("book-count@test.com", testPassword))
                }.body<LoginResponse>().token

            // Create category
            val category =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(CategoryCreateRequest("Book Count Test", "Testing book count"))
                }.body<Category>()

            // Initially should have 0 books
            var fetchedCategory = client.get("/categories/${category.id}").body<Category>()
            assertEquals(0, fetchedCategory.bookCount)

            // Add 3 books
            repeat(3) { i ->
                client.post("/books") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(
                        BookCreateRequest(
                            "Book $i",
                            "Author",
                            "Publisher",
                            2020,
                            category.id!!,
                        ),
                    )
                }
            }

            // Should now have 3 books
            fetchedCategory = client.get("/categories/${category.id}").body<Category>()
            assertEquals(3, fetchedCategory.bookCount)
        }

    @Test
    fun `test create category with invalid data`() =
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
                setBody(CustomerCreateRequest(testName, "invalid-cat@test.com", testPassword))
            }

            val token =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest("invalid-cat@test.com", testPassword))
                }.body<LoginResponse>().token

            // Test blank name
            val blankNameResponse =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(CategoryCreateRequest("   ", "Description"))
                }

            assertEquals(HttpStatusCode.BadRequest, blankNameResponse.status)

            // Test blank description
            val blankDescResponse =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(CategoryCreateRequest("Name", "   "))
                }

            assertEquals(HttpStatusCode.BadRequest, blankDescResponse.status)
        }

    @Test
    fun `test category operations without authentication`() =
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

            // Create category without auth should fail
            val createResponse =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    setBody(CategoryCreateRequest("Category", "Description"))
                }

            assertEquals(HttpStatusCode.Unauthorized, createResponse.status)

            // Reading categories should work (public)
            val getResponse = client.get("/categories")
            assertEquals(HttpStatusCode.OK, getResponse.status)
        }

    @Test
    fun `test update category with partial data`() =
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
                setBody(CustomerCreateRequest(testName, "partial-update@test.com", testPassword))
            }

            val token =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest("partial-update@test.com", testPassword))
                }.body<LoginResponse>().token

            // Create category
            val category =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(CategoryCreateRequest("Original Name", "Original Description"))
                }.body<Category>()

            // Update only name
            client.put("/categories/${category.id}") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(CategoryUpdateRequest(name = "New Name"))
            }

            var updated = client.get("/categories/${category.id}").body<Category>()
            assertEquals("New Name", updated.name)
            assertEquals("Original Description", updated.description) // Unchanged

            // Update only description
            client.put("/categories/${category.id}") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(CategoryUpdateRequest(description = "New Description"))
            }

            updated = client.get("/categories/${category.id}").body<Category>()
            assertEquals("New Name", updated.name) // Unchanged
            assertEquals("New Description", updated.description)
        }

    @Test
    fun `test get non-existent category returns 404`() =
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

            val response = client.get("/categories/99999")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }
}
