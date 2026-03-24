package com.fleetpulse.notifications

import com.fleetpulse.notifications.config.configureAuth
import com.fleetpulse.notifications.di.appModule
import com.fleetpulse.notifications.routes.healthRoutes
import com.fleetpulse.notifications.routes.notificationRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val mongoUri = environment.config.property("mongo.uri").getString()
    val allowedOrigins = environment.config.property("cors.allowed_origins").getString()

    install(Koin) {
        modules(appModule(mongoUri))
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-Internal-Key")
        allowCredentials = true
        allowedOrigins.split(",").forEach { allowHost(it.trim().removePrefix("http://").removePrefix("https://")) }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Internal server error"))
            )
        }
    }

    configureAuth()

    routing {
        healthRoutes()
        notificationRoutes()
    }
}
