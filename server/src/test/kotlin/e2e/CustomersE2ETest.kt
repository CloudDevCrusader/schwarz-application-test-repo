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
import com.schwarzdigital.domain.models.CustomerUpdateRequest
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class CustomersE2ETest {
    private val testEmail = "customers-e2e@test.com"
    private val testPassword = "password123"
    private val testName = "Customers E2E Test User"

    @Test
    fun `test complete customers CRUD flow`() =
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

            // Step 1: Register customer
            val registerResponse =
                client.post("/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(CustomerCreateRequest(testName, testEmail, testPassword))
                }

            assertEquals(HttpStatusCode.Created, registerResponse.status)
            val createdCustomer = registerResponse.body<CustomerResponse>()
            assertNotNull(createdCustomer.id)
            assertEquals(testEmail, createdCustomer.email)
            assertEquals(testName, createdCustomer.name)

            // Step 2: Login
            val loginResponse =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(testEmail, testPassword))
                }.body<LoginResponse>()

            val token = loginResponse.token
            assertNotNull(token)

            // Step 3: Get customer by ID
            val getResponse = client.get("/customers/${createdCustomer.id}") {
                bearerAuth(token)
            }

            assertEquals(HttpStatusCode.OK, getResponse.status)
            val fetchedCustomer = getResponse.body<CustomerResponse>()
            assertEquals(createdCustomer.id, fetchedCustomer.id)
            assertEquals(testEmail, fetchedCustomer.email)

            // Step 4: Update customer
            val updateResponse =
                client.put("/customers/${createdCustomer.id}") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(
                        CustomerUpdateRequest(
                            name = "Updated Name",
                            email = "updated@test.com",
                        ),
                    )
                }

            assertEquals(HttpStatusCode.OK, updateResponse.status)

            // Step 5: Verify update
            val updatedCustomer =
                client.get("/customers/${createdCustomer.id}") {
                    bearerAuth(token)
                }.body<CustomerResponse>()

            assertEquals("Updated Name", updatedCustomer.name)
            assertEquals("updated@test.com", updatedCustomer.email)

            // Step 6: Get all customers
            val allCustomersResponse =
                client.get("/customers") {
                    bearerAuth(token)
                }

            assertEquals(HttpStatusCode.OK, allCustomersResponse.status)
            val allCustomers = allCustomersResponse.body<List<CustomerResponse>>()
            assertTrue(allCustomers.any { it.id == createdCustomer.id })

            // Step 7: Delete customer
            val deleteResponse =
                client.delete("/customers/${createdCustomer.id}") {
                    bearerAuth(token)
                }

            assertEquals(HttpStatusCode.OK, deleteResponse.status)

            // Step 8: Verify deletion
            val notFoundResponse =
                client.get("/customers/${createdCustomer.id}") {
                    bearerAuth(token)
                }
            assertEquals(HttpStatusCode.NotFound, notFoundResponse.status)
        }

    @Test
    fun `test customer operations require authentication`() =
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

            // Get all customers without auth should fail
            val getAllResponse = client.get("/customers")
            assertEquals(HttpStatusCode.Unauthorized, getAllResponse.status)

            // Get customer by ID without auth should fail
            val getByIdResponse = client.get("/customers/1")
            assertEquals(HttpStatusCode.Unauthorized, getByIdResponse.status)

            // Update customer without auth should fail
            val updateResponse =
                client.put("/customers/1") {
                    contentType(ContentType.Application.Json)
                    setBody(CustomerUpdateRequest(name = "New Name"))
                }
            assertEquals(HttpStatusCode.Unauthorized, updateResponse.status)

            // Delete customer without auth should fail
            val deleteResponse = client.delete("/customers/1")
            assertEquals(HttpStatusCode.Unauthorized, deleteResponse.status)
        }

    @Test
    fun `test update customer with partial data`() =
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
                setBody(CustomerCreateRequest("Original Name", "partial@test.com", testPassword))
            }

            val loginResponse =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest("partial@test.com", testPassword))
                }.body<LoginResponse>()

            val token = loginResponse.token
            val customerId = loginResponse.customer.id

            // Update only name
            client.put("/customers/$customerId") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(CustomerUpdateRequest(name = "New Name"))
            }

            var updated =
                client.get("/customers/$customerId") {
                    bearerAuth(token)
                }.body<CustomerResponse>()

            assertEquals("New Name", updated.name)
            assertEquals("partial@test.com", updated.email) // Unchanged

            // Update only email
            client.put("/customers/$customerId") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(CustomerUpdateRequest(email = "newemail@test.com"))
            }

            updated =
                client.get("/customers/$customerId") {
                    bearerAuth(token)
                }.body<CustomerResponse>()

            assertEquals("New Name", updated.name) // Unchanged
            assertEquals("newemail@test.com", updated.email)
        }

    @Test
    fun `test update customer with invalid email`() =
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
                setBody(CustomerCreateRequest(testName, "validemail@test.com", testPassword))
            }

            val loginResponse =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest("validemail@test.com", testPassword))
                }.body<LoginResponse>()

            val token = loginResponse.token
            val customerId = loginResponse.customer.id

            // Try to update with invalid email
            val response =
                client.put("/customers/$customerId") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(CustomerUpdateRequest(email = "not-an-email"))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            val error = response.body<ErrorResponse>()
            assertEquals("VALIDATION_ERROR", error.error)
        }

    @Test
    fun `test update customer password`() =
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

            val email = "password-change@test.com"
            val oldPassword = "oldpassword123"
            val newPassword = "newpassword456"

            // Register
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(CustomerCreateRequest(testName, email, oldPassword))
            }

            // Login with old password
            var loginResponse =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(email, oldPassword))
                }.body<LoginResponse>()

            val token = loginResponse.token
            val customerId = loginResponse.customer.id

            // Update password
            client.put("/customers/$customerId") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(CustomerUpdateRequest(password = newPassword))
            }

            // Try login with old password - should fail
            val oldPasswordResponse =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(email, oldPassword))
                }
            assertEquals(HttpStatusCode.Unauthorized, oldPasswordResponse.status)

            // Login with new password - should succeed
            val newPasswordResponse =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(email, newPassword))
                }
            assertEquals(HttpStatusCode.OK, newPasswordResponse.status)
        }

    @Test
    fun `test get non-existent customer returns 404`() =
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

            // Register and login to get token
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(CustomerCreateRequest(testName, "notfound@test.com", testPassword))
            }

            val token =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest("notfound@test.com", testPassword))
                }.body<LoginResponse>().token

            val response =
                client.get("/customers/99999") {
                    bearerAuth(token)
                }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `test update non-existent customer returns 404`() =
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

            // Register and login to get token
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(CustomerCreateRequest(testName, "update-notfound@test.com", testPassword))
            }

            val token =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest("update-notfound@test.com", testPassword))
                }.body<LoginResponse>().token

            val response =
                client.put("/customers/99999") {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token)
                    setBody(CustomerUpdateRequest(name = "New Name"))
                }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }
}
