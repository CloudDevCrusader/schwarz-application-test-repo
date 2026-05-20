package com.schwarzdigitale

import com.schwarzdigitale.database.DatabaseFactory
import io.ktor.server.application.*

fun Application.configureDatabase() {
    DatabaseFactory.init(environment)
    log.info("Database initialized successfully")
}
