package com.schwarzdigital

import com.schwarzdigital.database.DatabaseFactory
import io.ktor.server.application.*

fun Application.configureDatabase() {
    DatabaseFactory.init(environment)
    log.info("Database initialized successfully")
}
