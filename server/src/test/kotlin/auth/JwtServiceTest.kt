package com.schwarzdigitale.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.schwarzdigitale.domain.models.Customer
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.mockk.every
import io.mockk.mockk
import java.util.*

class JwtServiceTest : StringSpec({
    
    "should generate a valid JWT token for a customer" {
        val environment = createMockEnvironment()
        val jwtService = JwtService(environment)
        
        val customer = Customer(
            id = 1,
            name = "John Doe",
            email = "john@example.com",
            passwordHash = "hashed"
        )
        
        val token = jwtService.generateToken(customer)
        
        token.shouldNotBeEmpty()
    }
    
    "generated token should contain customer claims" {
        val environment = createMockEnvironment()
        val jwtService = JwtService(environment)
        
        val customer = Customer(
            id = 42,
            name = "Jane Smith",
            email = "jane@example.com",
            passwordHash = "hashed"
        )
        
        val token = jwtService.generateToken(customer)
        
        val verifier = JWT.require(Algorithm.HMAC256(jwtService.getSecret()))
            .withIssuer(jwtService.getIssuer())
            .withAudience(jwtService.getAudience())
            .build()
        
        val decodedJWT = verifier.verify(token)
        
        decodedJWT.getClaim("id").asInt() shouldBe 42
        decodedJWT.getClaim("email").asString() shouldBe "jane@example.com"
        decodedJWT.getClaim("name").asString() shouldBe "Jane Smith"
    }
    
    "generated token should have correct issuer and audience" {
        val environment = createMockEnvironment()
        val jwtService = JwtService(environment)
        
        val customer = Customer(
            id = 1,
            name = "Test User",
            email = "test@example.com",
            passwordHash = "hashed"
        )
        
        val token = jwtService.generateToken(customer)
        
        val decodedJWT = JWT.decode(token)
        
        decodedJWT.issuer shouldBe "library-application"
        decodedJWT.audience shouldNotBe null
        decodedJWT.audience.first() shouldBe "library-users"
    }
    
    "generated token should have expiration time" {
        val environment = createMockEnvironment()
        val jwtService = JwtService(environment)
        
        val customer = Customer(
            id = 1,
            name = "Test User",
            email = "test@example.com",
            passwordHash = "hashed"
        )
        
        val beforeGeneration = Date()
        val token = jwtService.generateToken(customer)
        val afterGeneration = Date()
        
        val decodedJWT = JWT.decode(token)
        
        decodedJWT.expiresAt shouldNotBe null
        decodedJWT.expiresAt.after(beforeGeneration) shouldBe true
    }
    
    "should use default values when config is not provided" {
        val environment = createMockEnvironmentWithoutJwtConfig()
        val jwtService = JwtService(environment)
        
        jwtService.getSecret() shouldBe "default-secret-change-in-production"
        jwtService.getIssuer() shouldBe "library-app"
        jwtService.getAudience() shouldBe "library-users"
    }
})

private fun createMockEnvironment(): ApplicationEnvironment {
    val environment = mockk<ApplicationEnvironment>()
    val config = mockk<ApplicationConfig>()
    
    every { environment.config } returns config
    
    every { config.propertyOrNull("jwt.secret")?.getString() } returns "test-secret-key-min-256-bits"
    every { config.propertyOrNull("jwt.issuer")?.getString() } returns "library-application"
    every { config.propertyOrNull("jwt.audience")?.getString() } returns "library-users"
    every { config.propertyOrNull("jwt.validity")?.getString() } returns "3600000"
    
    return environment
}

private fun createMockEnvironmentWithoutJwtConfig(): ApplicationEnvironment {
    val environment = mockk<ApplicationEnvironment>()
    val config = mockk<ApplicationConfig>()
    
    every { environment.config } returns config
    
    every { config.propertyOrNull("jwt.secret")?.getString() } returns null
    every { config.propertyOrNull("jwt.issuer")?.getString() } returns null
    every { config.propertyOrNull("jwt.audience")?.getString() } returns null
    every { config.propertyOrNull("jwt.validity")?.getString() } returns null
    
    return environment
}
