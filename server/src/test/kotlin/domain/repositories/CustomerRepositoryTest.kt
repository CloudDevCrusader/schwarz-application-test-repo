package com.schwarzdigitale.domain.repositories

import com.schwarzdigitale.domain.models.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

class CustomerRepositoryTest : DescribeSpec({
    
    // Setup in-memory database for testing
    beforeSpec {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(Customers)
        }
    }
    
    afterSpec {
        transaction {
            SchemaUtils.drop(Customers)
        }
    }
    
    afterTest {
        transaction {
            Customers.deleteAll()
        }
    }
    
    describe("CustomerRepository.create") {
        
        it("should create a new customer") {
            val repository = CustomerRepository()
            val request = CustomerCreateRequest(
                name = "John Doe",
                email = "john@example.com",
                password = "password123"
            )
            
            val result = repository.create(request)
            
            result.id shouldNotBe null
            result.name shouldBe "John Doe"
            result.email shouldBe "john@example.com"
        }
        
        it("should hash the password") {
            val repository = CustomerRepository()
            val request = CustomerCreateRequest(
                name = "Jane Doe",
                email = "jane@example.com",
                password = "plainPassword"
            )
            
            repository.create(request)
            
            val customer = repository.findByEmail("jane@example.com")
            customer shouldNotBe null
            customer!!.passwordHash shouldNotBe "plainPassword"
            customer.passwordHash.shouldNotBeEmpty()
        }
    }
    
    describe("CustomerRepository.findById") {
        
        it("should find customer by id") {
            val repository = CustomerRepository()
            val created = repository.create(
                CustomerCreateRequest("Test User", "test@example.com", "password")
            )
            
            val found = repository.findById(created.id)
            
            found shouldNotBe null
            found!!.id shouldBe created.id
            found.name shouldBe "Test User"
            found.email shouldBe "test@example.com"
        }
        
        it("should return null for non-existent id") {
            val repository = CustomerRepository()
            
            val found = repository.findById(99999)
            
            found shouldBe null
        }
    }
    
    describe("CustomerRepository.findByEmail") {
        
        it("should find customer by email") {
            val repository = CustomerRepository()
            repository.create(
                CustomerCreateRequest("Email User", "email@example.com", "password")
            )
            
            val found = repository.findByEmail("email@example.com")
            
            found shouldNotBe null
            found!!.email shouldBe "email@example.com"
            found.name shouldBe "Email User"
        }
        
        it("should return null for non-existent email") {
            val repository = CustomerRepository()
            
            val found = repository.findByEmail("nonexistent@example.com")
            
            found shouldBe null
        }
    }
    
    describe("CustomerRepository.findAll") {
        
        it("should return all customers") {
            val repository = CustomerRepository()
            repository.create(CustomerCreateRequest("User 1", "user1@example.com", "password"))
            repository.create(CustomerCreateRequest("User 2", "user2@example.com", "password"))
            repository.create(CustomerCreateRequest("User 3", "user3@example.com", "password"))
            
            val all = repository.findAll()
            
            all shouldHaveSize 3
        }
        
        it("should return empty list when no customers exist") {
            val repository = CustomerRepository()
            
            val all = repository.findAll()
            
            all shouldHaveSize 0
        }
    }
    
    describe("CustomerRepository.update") {
        
        it("should update customer name") {
            val repository = CustomerRepository()
            val created = repository.create(
                CustomerCreateRequest("Original Name", "update@example.com", "password")
            )
            
            val updated = repository.update(
                created.id,
                CustomerUpdateRequest(name = "New Name")
            )
            
            updated shouldBe true
            val found = repository.findById(created.id)
            found!!.name shouldBe "New Name"
        }
        
        it("should update customer email") {
            val repository = CustomerRepository()
            val created = repository.create(
                CustomerCreateRequest("Test", "old@example.com", "password")
            )
            
            repository.update(
                created.id,
                CustomerUpdateRequest(email = "new@example.com")
            )
            
            val found = repository.findById(created.id)
            found!!.email shouldBe "new@example.com"
        }
        
        it("should return false for non-existent customer") {
            val repository = CustomerRepository()
            
            val updated = repository.update(
                99999,
                CustomerUpdateRequest(name = "New Name")
            )
            
            updated shouldBe false
        }
    }
    
    describe("CustomerRepository.delete") {
        
        it("should delete customer") {
            val repository = CustomerRepository()
            val created = repository.create(
                CustomerCreateRequest("To Delete", "delete@example.com", "password")
            )
            
            val deleted = repository.delete(created.id)
            
            deleted shouldBe true
            repository.findById(created.id) shouldBe null
        }
        
        it("should return false for non-existent customer") {
            val repository = CustomerRepository()
            
            val deleted = repository.delete(99999)
            
            deleted shouldBe false
        }
    }
    
    describe("CustomerRepository.verifyPassword") {
        
        it("should verify correct password") {
            val repository = CustomerRepository()
            repository.create(
                CustomerCreateRequest("Verify User", "verify@example.com", "correctPassword")
            )
            
            val result = repository.verifyPassword("verify@example.com", "correctPassword")
            
            result shouldBe true
        }
        
        it("should reject incorrect password") {
            val repository = CustomerRepository()
            repository.create(
                CustomerCreateRequest("Verify User", "verify2@example.com", "correctPassword")
            )
            
            val result = repository.verifyPassword("verify2@example.com", "wrongPassword")
            
            result shouldBe false
        }
        
        it("should return false for non-existent email") {
            val repository = CustomerRepository()
            
            val result = repository.verifyPassword("nonexistent@example.com", "anyPassword")
            
            result shouldBe false
        }
    }
})
