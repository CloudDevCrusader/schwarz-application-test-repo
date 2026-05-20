package com.schwarzdigitale.database

import com.schwarzdigitale.domain.models.Books
import com.schwarzdigitale.domain.models.Categories
import com.schwarzdigitale.domain.models.Customers
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(environment: ApplicationEnvironment) {
        val embedded = environment.config.propertyOrNull("database.embedded")?.getString()?.toBoolean() ?: true
        
        val database = if (embedded) {
            Database.connect(
                url = "jdbc:h2:mem:library;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                driver = "org.h2.Driver",
                user = "root",
                password = ""
            )
        } else {
            val url = environment.config.property("database.url").getString()
            val user = environment.config.property("database.user").getString()
            val password = environment.config.property("database.password").getString()
            
            Database.connect(
                url = url,
                driver = "org.postgresql.Driver",
                user = user,
                password = password
            )
        }
        
        transaction(database) {
            SchemaUtils.create(Customers, Categories, Books)
        }
    }
}
