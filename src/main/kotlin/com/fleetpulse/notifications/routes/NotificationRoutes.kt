package com.fleetpulse.notifications.routes

import com.fleetpulse.notifications.config.userId
import com.fleetpulse.notifications.models.SendRequest
import com.fleetpulse.notifications.models.toResponse
import com.fleetpulse.notifications.service.NotificationService
import com.fleetpulse.notifications.service.PaginatedResult
import com.fleetpulse.notifications.models.NotificationResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.notificationRoutes() {
    val service by inject<NotificationService>()

    // Internal endpoint — called by Node.js API with X-Internal-Key
    authenticate("internal") {
        post("/api/v1/notifications/send") {
            val request = call.receive<SendRequest>()
            val notification = service.create(request)
            call.respond(HttpStatusCode.Created, mapOf("data" to notification.toResponse()))
        }

        post("/api/v1/notifications/send-bulk") {
            val requests = call.receive<List<SendRequest>>()
            val notifications = service.bulkCreate(requests)
            call.respond(HttpStatusCode.Created, mapOf("data" to notifications.map { it.toResponse() }))
        }
    }

    // User-facing endpoints — JWT auth
    authenticate("jwt") {
        route("/api/v1/notifications") {
            get {
                val userId = call.principal<JWTPrincipal>()!!.userId()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val result = service.findByUserId(userId, page, limit)
                call.respond(PaginatedResult(
                    data = result.data.map { it.toResponse() },
                    meta = result.meta
                ))
            }

            get("/unread-count") {
                val userId = call.principal<JWTPrincipal>()!!.userId()
                val count = service.getUnreadCount(userId)
                call.respond(mapOf("data" to mapOf("count" to count)))
            }

            patch("/{id}/read") {
                val userId = call.principal<JWTPrincipal>()!!.userId()
                val id = call.parameters["id"] ?: return@patch call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing notification ID")
                )
                val updated = service.markAsRead(id, userId)
                if (updated) {
                    call.respond(mapOf("data" to mapOf("message" to "Marked as read")))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Notification not found"))
                }
            }

            patch("/read-all") {
                val userId = call.principal<JWTPrincipal>()!!.userId()
                val count = service.markAllAsRead(userId)
                call.respond(mapOf("data" to mapOf("marked" to count)))
            }

            delete("/{id}") {
                val userId = call.principal<JWTPrincipal>()!!.userId()
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing notification ID")
                )
                val deleted = service.delete(id, userId)
                if (deleted) {
                    call.respond(mapOf("data" to mapOf("message" to "Deleted")))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Notification not found"))
                }
            }
        }
    }
}
