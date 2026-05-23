package com.schwarzdigital

import com.schwarzdigital.auth.LoginRequest
import com.schwarzdigital.auth.LoginResponse
import com.schwarzdigital.auth.configureAuth
import com.schwarzdigital.domain.models.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class LibraryApiTest {
    private val testEmail = "test@example.com"
    private val testPassword = "password123"
    private val testName = "Test User"

    @Test
    fun testRootEndpoint() =
        testApplication {
            application {
                configureSerialization()
                configureDatabase()
                configureAuth()
                configureRouting()
            }
            val response = client.get("/")
            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("Library API"))
        }

    @Test
    fun testHealthEndpoint() =
        testApplication {
            application {
                configureSerialization()
                configureDatabase()
                configureAuth()
                configureRouting()
            }
            val response = client.get("/health")
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun testRegisterCustomer() =
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

            val response =
                client.post("/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        CustomerCreateRequest(
                            name = testName,
                            email = "register@test.com", // Use unique email for this test
                            password = testPassword,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.Created, response.status)
            val customerResponse = response.body<CustomerResponse>()
            assertEquals("register@test.com", customerResponse.email)
            assertEquals(testName, customerResponse.name)
        }

    @Test
    fun testRegisterWithInvalidEmail() =
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

            val response =
                client.post("/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        CustomerCreateRequest(
                            name = testName,
                            email = "invalid-email",
                            password = testPassword,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun testLoginSuccess() =
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

            val loginEmail = "login@test.com" // Use unique email

            // First register
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    CustomerCreateRequest(
                        name = testName,
                        email = loginEmail,
                        password = testPassword,
                    ),
                )
            }

            // Then login
            val response =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        LoginRequest(
                            email = loginEmail,
                            password = testPassword,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val loginResponse = response.body<LoginResponse>()
            assertNotNull(loginResponse.token)
            assertEquals(loginEmail, loginResponse.customer.email)
        }

    @Test
    fun testLoginWithWrongPassword() =
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

            // First register
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    CustomerCreateRequest(
                        name = testName,
                        email = "wrong@example.com",
                        password = testPassword,
                    ),
                )
            }

            // Try login with wrong password
            val response =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        LoginRequest(
                            email = "wrong@example.com",
                            password = "wrongpassword",
                        ),
                    )
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun testCreateCategoryWithoutAuth() =
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

            val response =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        CategoryCreateRequest(
                            name = "Fiction",
                            description = "Fiction books",
                        ),
                    )
                }

            // Should fail because no authentication
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun testCreateCategoryWithAuth() =
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
                setBody(
                    CustomerCreateRequest(
                        name = testName,
                        email = "category@test.com",
                        password = testPassword,
                    ),
                )
            }

            val loginResponse =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        LoginRequest(
                            email = "category@test.com",
                            password = testPassword,
                        ),
                    )
                }.body<LoginResponse>()

            val token = loginResponse.token

            // Create category with auth
            val response =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(
                        CategoryCreateRequest(
                            name = "Science Fiction",
                            description = "Science fiction books",
                        ),
                    )
                }

            assertEquals(HttpStatusCode.Created, response.status)
            val category = response.body<Category>()
            assertEquals("Science Fiction", category.name)
        }

    @Test
    fun testGetCategoriesPublic() =
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

            // Should work without authentication
            val response = client.get("/categories")
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun testCreateAndGetBook() =
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
                setBody(
                    CustomerCreateRequest(
                        name = testName,
                        email = "book@test.com",
                        password = testPassword,
                    ),
                )
            }

            val loginResponse =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        LoginRequest(
                            email = "book@test.com",
                            password = testPassword,
                        ),
                    )
                }.body<LoginResponse>()

            val token = loginResponse.token

            // Create category first
            val categoryResponse =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(
                        CategoryCreateRequest(
                            name = "Fantasy",
                            description = "Fantasy books",
                        ),
                    )
                }.body<Category>()

            // Create book
            val bookResponse =
                client.post("/books") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(
                        BookCreateRequest(
                            title = "The Hobbit",
                            author = "J.R.R. Tolkien",
                            publisher = "Allen & Unwin",
                            publishingYear = 1937,
                            categoryId = categoryResponse.id!!,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.Created, bookResponse.status)
            val book = bookResponse.body<Book>()
            assertEquals("The Hobbit", book.title)
            assertEquals("Fantasy", book.categoryName)

            // Get book without auth (public)
            val getResponse = client.get("/books/${book.id}")
            assertEquals(HttpStatusCode.OK, getResponse.status)
        }

    @Test
    fun testDeleteBookWithAuth() =
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

            // Setup: Register, login, create category and book
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    CustomerCreateRequest(
                        name = testName,
                        email = "delete@test.com",
                        password = testPassword,
                    ),
                )
            }

            val loginResponse =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        LoginRequest(
                            email = "delete@test.com",
                            password = testPassword,
                        ),
                    )
                }.body<LoginResponse>()

            val token = loginResponse.token

            val categoryResponse =
                client.post("/categories") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(
                        CategoryCreateRequest(
                            name = "Mystery",
                            description = "Mystery books",
                        ),
                    )
                }.body<Category>()

            val bookResponse =
                client.post("/books") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(
                        BookCreateRequest(
                            title = "Test Book",
                            author = "Test Author",
                            publisher = "Test Publisher",
                            publishingYear = 2024,
                            categoryId = categoryResponse.id!!,
                        ),
                    )
                }.body<Book>()

            // Delete the book
            val deleteResponse =
                client.delete("/books/${bookResponse.id}") {
                    bearerAuth(token)
                }

            assertEquals(HttpStatusCode.OK, deleteResponse.status)

            // Verify book is deleted
            val getResponse = client.get("/books/${bookResponse.id}")
            assertEquals(HttpStatusCode.NotFound, getResponse.status)
        }
}
