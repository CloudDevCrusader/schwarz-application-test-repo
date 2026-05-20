package com.schwarzdigitale.domain.repositories

import at.favre.lib.crypto.bcrypt.BCrypt
import com.schwarzdigitale.domain.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class CustomerRepository {
    
    fun create(request: CustomerCreateRequest): CustomerResponse = transaction {
        val passwordHash = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())
        
        val id = Customers.insertAndGetId {
            it[name] = request.name
            it[email] = request.email
            it[Customers.passwordHash] = passwordHash
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        
        CustomerResponse(
            id = id.value,
            name = request.name,
            email = request.email
        )
    }
    
    fun findAll(): List<CustomerResponse> = transaction {
        Customers.selectAll().map { rowToCustomerResponse(it) }
    }
    
    fun findById(id: Int): CustomerResponse? = transaction {
        Customers.selectAll().where { Customers.id eq id }
            .mapNotNull { rowToCustomerResponse(it) }
            .singleOrNull()
    }
    
    fun findByEmail(email: String): Customer? = transaction {
        Customers.selectAll().where { Customers.email eq email }
            .mapNotNull { rowToCustomer(it) }
            .singleOrNull()
    }
    
    fun update(id: Int, request: CustomerUpdateRequest): Boolean = transaction {
        val updateCount = Customers.update({ Customers.id eq id }) {
            request.name?.let { name -> it[Customers.name] = name }
            request.email?.let { email -> it[Customers.email] = email }
            request.password?.let { password -> 
                it[passwordHash] = BCrypt.withDefaults().hashToString(12, password.toCharArray())
            }
            it[updatedAt] = LocalDateTime.now()
        }
        updateCount > 0
    }
    
    fun delete(id: Int): Boolean = transaction {
        val deleteCount = Customers.deleteWhere { Customers.id eq id }
        deleteCount > 0
    }
    
    fun verifyPassword(email: String, password: String): Boolean = transaction {
        val customer = findByEmail(email) ?: return@transaction false
        val result = BCrypt.verifyer().verify(password.toCharArray(), customer.passwordHash)
        result.verified
    }
    
    private fun rowToCustomerResponse(row: ResultRow): CustomerResponse =
        CustomerResponse(
            id = row[Customers.id].value,
            name = row[Customers.name],
            email = row[Customers.email]
        )
    
    private fun rowToCustomer(row: ResultRow): Customer =
        Customer(
            id = row[Customers.id].value,
            name = row[Customers.name],
            email = row[Customers.email],
            passwordHash = row[Customers.passwordHash]
        )
}
