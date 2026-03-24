package com.fleetpulse.notifications.models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class NotificationTest : FunSpec({
    test("Notification should have required fields") {
        val notification = Notification(
            userId = "user123",
            type = NotificationType.TRIP_ASSIGNED,
            title = "New Trip",
            message = "You have been assigned trip FP-20260323-A1B2",
        )
        notification.title shouldBe "New Trip"
        notification.read shouldBe false
        notification.type shouldBe NotificationType.TRIP_ASSIGNED
        notification.userId shouldBe "user123"
        notification.id shouldNotBe null
    }

    test("Notification should have default read=false") {
        val n = Notification(
            userId = "u1",
            type = NotificationType.GENERAL,
            title = "Test",
            message = "Test message"
        )
        n.read shouldBe false
    }

    test("Notification should accept optional tripId") {
        val n = Notification(
            userId = "u1",
            type = NotificationType.TRIP_COMPLETED,
            title = "Done",
            message = "Trip done",
            tripId = "trip123"
        )
        n.tripId shouldBe "trip123"
    }

    test("SendRequest should hold all fields") {
        val req = SendRequest(
            userId = "u1",
            type = NotificationType.MAINTENANCE_DUE,
            title = "Maintenance",
            message = "Due soon"
        )
        req.type shouldBe NotificationType.MAINTENANCE_DUE
        req.tripId shouldBe null
    }
})
