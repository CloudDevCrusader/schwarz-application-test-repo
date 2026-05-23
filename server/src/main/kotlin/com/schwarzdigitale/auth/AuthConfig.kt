package com.schwarzdigital.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureAuth() {
    val jwtService = JwtService(environment)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Library Application"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtService.getSecret()))
                    .withAudience(jwtService.getAudience())
                    .withIssuer(jwtService.getIssuer())
                    .build(),
            )
            validate { credential ->
                if (credential.payload.getClaim("email").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

// Extension to get user ID from JWT
val ApplicationCall.userId: Int?
    get() = principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()

val ApplicationCall.userEmail: String?
    get() = principal<JWTPrincipal>()?.payload?.getClaim("email")?.asString()
