package com.schwarzdigital.e2e

import com.schwarzdigital.auth.ErrorResponse
import com.schwarzdigital.auth.LoginRequest
import com.schwarzdigital.auth.LoginResponse
import com.schwarzdigital.auth.configureAuth
import com.schwarzdigital.configureDatabase
import com.schwarzdigital.configureRouting
import com.schwarzdigital.configureSerialization
import com.schwarzdigital.domain.models.CustomerCreateRequest
import com.schwarzdigital.domain.models.CustomerResponse
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class AuthenticationE2ETest {
    private val testEmail = "auth-e2e@test.com"
    private val testPassword = "password123"
    private val testName = "Auth E2E Test User"

    @Test
    fun `test complete registration and login flow`() =
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

            // Step 1: Register
            val registerResponse =
                client.post("/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(CustomerCreateRequest(testName, testEmail, testPassword))
                }

            assertEquals(HttpStatusCode.Created, registerResponse.status)
            val customerResponse = registerResponse.body<CustomerResponse>()
            assertNotNull(customerResponse.id)
            assertEquals(testEmail, customerResponse.email)
            assertEquals(testName, customerResponse.name)

            // Step 2: Login
            val loginResponse =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(testEmail, testPassword))
                }

            assertEquals(HttpStatusCode.OK, loginResponse.status)
            val loginData = loginResponse.body<LoginResponse>()
            assertNotNull(loginData.token)
            assertTrue(loginData.token.isNotEmpty())
            assertEquals(testEmail, loginData.customer.email)
            assertEquals(customerResponse.id, loginData.customer.id)

            // Step 3: Use token for authenticated request
            val authenticatedResponse =
                client.get("/customers") {
                    bearerAuth(loginData.token)
                }

            assertEquals(HttpStatusCode.OK, authenticatedResponse.status)
        }

    @Test
    fun `test registration with duplicate email fails`() =
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

            val email = "duplicate@test.com"

            // First registration
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(CustomerCreateRequest(testName, email, testPassword))
            }

            // Second registration with same email
            val response =
                client.post("/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(CustomerCreateRequest("Another Name", email, testPassword))
                }

            assertEquals(HttpStatusCode.Conflict, response.status)
            val error = response.body<ErrorResponse>()
            assertEquals("EMAIL_EXISTS", error.error)
        }

    @Test
    fun `test registration with invalid email format`() =
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

            val invalidEmails =
                listOf(
                    "not-an-email",
                    "@example.com",
                    "user@",
                    "user@.com",
                    "user..name@example.com",
                )

            invalidEmails.forEach { email ->
                val response =
                    client.post("/auth/register") {
                        contentType(ContentType.Application.Json)
                        setBody(CustomerCreateRequest(testName, email, testPassword))
                    }

                assertEquals(HttpStatusCode.BadRequest, response.status)
                val error = response.body<ErrorResponse>()
                assertEquals("VALIDATION_ERROR", error.error)
            }
        }

    @Test
    fun `test registration with short password fails`() =
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
                    setBody(CustomerCreateRequest(testName, "short@test.com", "12345"))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            val error = response.body<ErrorResponse>()
            assertEquals("VALIDATION_ERROR", error.error)
            assertTrue(error.message.contains("6 characters", ignoreCase = true))
        }

    @Test
    fun `test registration with short name fails`() =
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
                    setBody(CustomerCreateRequest("A", "shortname@test.com", testPassword))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            val error = response.body<ErrorResponse>()
            assertEquals("VALIDATION_ERROR", error.error)
        }

    @Test
    fun `test login with wrong password fails`() =
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

            val email = "wrongpass@test.com"

            // Register
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(CustomerCreateRequest(testName, email, testPassword))
            }

            // Try login with wrong password
            val response =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(email, "wrongpassword"))
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            val error = response.body<ErrorResponse>()
            assertEquals("INVALID_CREDENTIALS", error.error)
        }

    @Test
    fun `test login with non-existent email fails`() =
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
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest("nonexistent@test.com", testPassword))
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            val error = response.body<ErrorResponse>()
            assertEquals("INVALID_CREDENTIALS", error.error)
        }

    @Test
    fun `test authenticated endpoints reject invalid token`() =
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

            val invalidToken = "invalid.jwt.token"

            // Try to access protected endpoint with invalid token
            val response =
                client.get("/customers") {
                    bearerAuth(invalidToken)
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `test authenticated endpoints work with valid token`() =
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

            // Register and login to get valid token
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(CustomerCreateRequest(testName, "validtoken@test.com", testPassword))
            }

            val loginResponse =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest("validtoken@test.com", testPassword))
                }.body<LoginResponse>()

            val token = loginResponse.token

            // Test multiple authenticated endpoints
            val customersResponse =
                client.get("/customers") {
                    bearerAuth(token)
                }
            assertEquals(HttpStatusCode.OK, customersResponse.status)

            val customerByIdResponse =
                client.get("/customers/${loginResponse.customer.id}") {
                    bearerAuth(token)
                }
            assertEquals(HttpStatusCode.OK, customerByIdResponse.status)
        }

    @Test
    fun `test public endpoints work without authentication`() =
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

            // Test public endpoints
            val rootResponse = client.get("/")
            assertEquals(HttpStatusCode.OK, rootResponse.status)

            val healthResponse = client.get("/health")
            assertEquals(HttpStatusCode.OK, healthResponse.status)

            val booksResponse = client.get("/books")
            assertEquals(HttpStatusCode.OK, booksResponse.status)

            val categoriesResponse = client.get("/categories")
            assertEquals(HttpStatusCode.OK, categoriesResponse.status)
        }

    @Test
    fun `test JWT token contains customer information`() =
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

            val email = "tokeninfo@test.com"

            // Register
            val registerResponse =
                client.post("/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(CustomerCreateRequest(testName, email, testPassword))
                }.body<CustomerResponse>()

            // Login
            val loginResponse =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(email, testPassword))
                }.body<LoginResponse>()

            // Verify token contains customer info
            assertNotNull(loginResponse.token)
            assertEquals(registerResponse.id, loginResponse.customer.id)
            assertEquals(email, loginResponse.customer.email)
            assertEquals(testName, loginResponse.customer.name)
        }
}
