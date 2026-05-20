package com.schwarzdigitale.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.schwarzdigitale.domain.models.Customer
import io.ktor.server.application.*
import java.util.*

class JwtService(private val environment: ApplicationEnvironment) {
    private val secret = environment.config.propertyOrNull("jwt.secret")?.getString() ?: "default-secret-change-in-production"
    private val issuer = environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "library-app"
    private val audience = environment.config.propertyOrNull("jwt.audience")?.getString() ?: "library-users"
    private val validityInMs = environment.config.propertyOrNull("jwt.validity")?.getString()?.toLong() ?: 3_600_000 // 1 hour
    
    fun generateToken(customer: Customer): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("id", customer.id)
            .withClaim("email", customer.email)
            .withClaim("name", customer.name)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(Algorithm.HMAC256(secret))
    }
    
    fun getSecret(): String = secret
    fun getIssuer(): String = issuer
    fun getAudience(): String = audience
}
