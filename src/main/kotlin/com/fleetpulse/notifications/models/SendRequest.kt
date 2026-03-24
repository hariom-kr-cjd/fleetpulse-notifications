package com.fleetpulse.notifications.models

import kotlinx.serialization.Serializable

@Serializable
data class SendRequest(
    val userId: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val tripId: String? = null
)
