package com.schwarzdigital

import com.schwarzdigital.auth.configureAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.*

class ServerTest {
    @Test
    fun `test root endpoint`() =
        testApplication {
            application {
                configureSerialization()
                configureStatusPages()
                configureAuth()
                configureDatabase()
                configureRouting()
            }
            assertEquals(HttpStatusCode.OK, client.get("/").status)
        }
}
