package com.schwarzdigital

import com.asyncapi.kotlinasyncapi.context.service.AsyncApiExtension
import com.asyncapi.kotlinasyncapi.ktor.AsyncApiPlugin
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureHttp() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("MyCustomHeader")
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(AsyncApiPlugin) {
        extension =
            AsyncApiExtension.builder {
                info {
                    title("Library Management API")
                    version("1.0.0")
                    description("A comprehensive Library Management System API for managing books, categories, and customers")
                }
            }
    }

    routing {
        // Serve OpenAPI specification
        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml") {
            // Serves the OpenAPI specification at /openapi
        }

        // Serve Swagger UI
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml") {
            // Serves the Swagger UI at /swagger
        }

        // Alternative: Serve Swagger UI at /api-docs
        swaggerUI(path = "api-docs", swaggerFile = "openapi/documentation.yaml") {
            // Serves the Swagger UI at /api-docs for convenience
        }
    }
}
