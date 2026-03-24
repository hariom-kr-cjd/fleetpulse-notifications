package com.fleetpulse.notifications.models

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val read: Boolean,
    val tripId: String? = null,
    val createdAt: String,
    val updatedAt: String
)

fun Notification.toResponse() = NotificationResponse(
    id = id.toString(),
    userId = userId,
    type = type,
    title = title,
    message = message,
    read = read,
    tripId = tripId,
    createdAt = createdAt.toInstant().toString(),
    updatedAt = updatedAt.toInstant().toString()
)
