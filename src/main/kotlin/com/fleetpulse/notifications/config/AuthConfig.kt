package com.fleetpulse.notifications.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureAuth() {
    val jwtSecret = environment.config.property("auth.jwt_secret").getString()
    val internalApiKey = environment.config.property("auth.internal_api_key").getString()

    install(Authentication) {
        jwt("jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId")?.asString()
                val role = credential.payload.getClaim("role")?.asString()
                if (userId != null && role != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or expired token"))
            }
        }

        bearer("internal") {
            authenticate { tokenCredential ->
                if (tokenCredential.token == internalApiKey) {
                    UserIdPrincipal("internal-service")
                } else {
                    null
                }
            }
        }
    }
}

fun JWTPrincipal.userId(): String = payload.getClaim("userId").asString()
fun JWTPrincipal.role(): String = payload.getClaim("role").asString()
