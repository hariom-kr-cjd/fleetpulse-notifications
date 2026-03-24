package com.fleetpulse.notifications.config

import com.fleetpulse.notifications.models.Notification
import io.ktor.server.application.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class DatabaseConfig(mongoUri: String, dbName: String = "fleetpulse") {
    val database: CoroutineDatabase
    val notifications: CoroutineCollection<Notification>

    init {
        val client = KMongo.createClient(mongoUri).coroutine
        database = client.getDatabase(dbName)
        notifications = database.getCollection<Notification>("notifications")
    }
}

fun Application.getDatabaseConfig(): DatabaseConfig {
    val mongoUri = environment.config.property("mongo.uri").getString()
    return DatabaseConfig(mongoUri)
}
