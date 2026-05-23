package com.schwarzdigitale.database

import com.schwarzdigitale.domain.models.Books
import com.schwarzdigitale.domain.models.Categories
import com.schwarzdigitale.domain.models.Customers
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(environment: ApplicationEnvironment) {
        val embedded = environment.config.propertyOrNull("database.embedded")?.getString()?.toBoolean() ?: true
        
        val database = if (embedded) {
            // Use H2 for embedded/testing
            val db = Database.connect(
                url = "jdbc:h2:mem:library;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                driver = "org.h2.Driver",
                user = "root",
                password = ""
            )
            
            // For H2, use Exposed's schema creation
            transaction(db) {
                SchemaUtils.create(Customers, Categories, Books)
            }
            
            db
        } else {
            // Use PostgreSQL with Flyway migrations
            val url = environment.config.property("database.url").getString()
            val user = environment.config.property("database.user").getString()
            val password = environment.config.property("database.password").getString()
            
            // Run Flyway migrations
            runMigrations(url, user, password)
            
            // Connect to database
            Database.connect(
                url = url,
                driver = "org.postgresql.Driver",
                user = user,
                password = password
            )
        }
    }
    
    private fun runMigrations(url: String, user: String, password: String) {
        try {
            val flyway = Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .validateOnMigrate(true)
                .load()
            
            val info = flyway.info()
            println("=== Flyway Migration Info ===")
            println("Current version: ${info.current()?.version ?: "No version"}")
            println("Pending migrations: ${info.pending().size}")
            
            val result = flyway.migrate()
            
            println("=== Migration Results ===")
            println("Migrations executed: ${result.migrationsExecuted}")
            println("Success: ${result.success}")
            if (result.warnings.isNotEmpty()) {
                println("Warnings: ${result.warnings}")
            }
            
        } catch (e: Exception) {
            println("ERROR: Flyway migration failed: ${e.message}")
            throw e
        }
    }
}
