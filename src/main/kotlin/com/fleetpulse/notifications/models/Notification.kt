package com.fleetpulse.notifications.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.util.Date

@Serializable
enum class NotificationType {
    @SerialName("trip_assigned") TRIP_ASSIGNED,
    @SerialName("trip_completed") TRIP_COMPLETED,
    @SerialName("maintenance_due") MAINTENANCE_DUE,
    @SerialName("general") GENERAL
}

@Serializable
data class Notification(
    @Contextual
    @SerialName("_id")
    val id: ObjectId = ObjectId(),
    val userId: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val read: Boolean = false,
    val tripId: String? = null,
    @Contextual
    val createdAt: Date = Date(),
    @Contextual
    val updatedAt: Date = Date()
)
