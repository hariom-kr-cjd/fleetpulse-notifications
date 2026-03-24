package com.fleetpulse.notifications.service

import com.fleetpulse.notifications.EmbeddedMongo
import com.fleetpulse.notifications.config.DatabaseConfig
import com.fleetpulse.notifications.models.NotificationType
import com.fleetpulse.notifications.models.SendRequest
import com.fleetpulse.notifications.repository.NotificationRepository
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NotificationServiceTest : FunSpec({
    var running: TransitionWalker.ReachedState<RunningMongodProcess>? = null
    var service: NotificationService? = null

    beforeSpec {
        running = EmbeddedMongo.start()
        val uri = EmbeddedMongo.getConnectionString(running!!)
        val dbConfig = DatabaseConfig(uri, "fleetpulse-test")
        val repo = NotificationRepository(dbConfig.notifications)
        service = NotificationService(repo)
    }

    afterSpec {
        running?.close()
    }

    test("create should return notification") {
        val req = SendRequest("user1", NotificationType.TRIP_ASSIGNED, "New Trip", "Assigned to you")
        val n = service!!.create(req)
        n.userId shouldBe "user1"
        n.title shouldBe "New Trip"
        n.read shouldBe false
    }

    test("findByUserId should return paginated results") {
        val userId = "paginateUser"
        repeat(5) {
            service!!.create(SendRequest(userId, NotificationType.GENERAL, "N$it", "Msg $it"))
        }
        val result = service!!.findByUserId(userId, page = 1, limit = 3)
        result.data.size shouldBe 3
        result.meta.total shouldBe 5
        result.meta.pages shouldBe 2
    }

    test("getUnreadCount should return count of unread notifications") {
        val userId = "countUser"
        repeat(3) {
            service!!.create(SendRequest(userId, NotificationType.GENERAL, "N$it", "Msg"))
        }
        service!!.getUnreadCount(userId) shouldBe 3
    }

    test("markAsRead should mark notification as read") {
        val req = SendRequest("readUser", NotificationType.GENERAL, "Read Test", "Msg")
        val n = service!!.create(req)
        service!!.markAsRead(n.id.toString(), "readUser") shouldBe true
        service!!.getUnreadCount("readUser") shouldBe 0
    }

    test("markAllAsRead should mark all unread as read") {
        val userId = "readAllUser"
        repeat(4) {
            service!!.create(SendRequest(userId, NotificationType.GENERAL, "N$it", "Msg"))
        }
        val marked = service!!.markAllAsRead(userId)
        marked shouldBe 4
        service!!.getUnreadCount(userId) shouldBe 0
    }

    test("delete should remove notification") {
        val req = SendRequest("deleteUser", NotificationType.GENERAL, "Del Test", "Msg")
        val n = service!!.create(req)
        service!!.delete(n.id.toString(), "deleteUser") shouldBe true
        val result = service!!.findByUserId("deleteUser")
        result.data.size shouldBe 0
    }

    test("bulkCreate should create multiple notifications") {
        val requests = listOf(
            SendRequest("bulkUser", NotificationType.GENERAL, "B1", "Msg1"),
            SendRequest("bulkUser", NotificationType.TRIP_ASSIGNED, "B2", "Msg2"),
            SendRequest("bulkUser", NotificationType.MAINTENANCE_DUE, "B3", "Msg3"),
        )
        val created = service!!.bulkCreate(requests)
        created.size shouldBe 3
        service!!.findByUserId("bulkUser").meta.total shouldBe 3
    }
})
