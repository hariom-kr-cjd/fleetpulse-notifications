package com.fleetpulse.notifications.service

import com.fleetpulse.notifications.models.Notification
import com.fleetpulse.notifications.models.NotificationType
import com.fleetpulse.notifications.models.SendRequest
import com.fleetpulse.notifications.repository.NotificationRepository
import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResult<T>(
    val data: List<T>,
    val meta: PaginationMeta
)

@Serializable
data class PaginationMeta(
    val total: Long,
    val page: Int,
    val limit: Int,
    val pages: Int
)

class NotificationService(private val repository: NotificationRepository) {

    suspend fun create(request: SendRequest): Notification {
        val notification = Notification(
            userId = request.userId,
            type = request.type,
            title = request.title,
            message = request.message,
            tripId = request.tripId
        )
        return repository.create(notification)
    }

    suspend fun findByUserId(userId: String, page: Int = 1, limit: Int = 20): PaginatedResult<Notification> {
        val skip = (page - 1) * limit
        val data = repository.findByUserId(userId, skip, limit)
        val total = repository.countByUserId(userId)
        return PaginatedResult(
            data = data,
            meta = PaginationMeta(
                total = total,
                page = page,
                limit = limit,
                pages = if (total == 0L) 0 else ((total + limit - 1) / limit).toInt()
            )
        )
    }

    suspend fun getUnreadCount(userId: String): Long {
        return repository.getUnreadCount(userId)
    }

    suspend fun markAsRead(id: String, userId: String): Boolean {
        return repository.markAsRead(id, userId)
    }

    suspend fun markAllAsRead(userId: String): Long {
        return repository.markAllAsRead(userId)
    }

    suspend fun delete(id: String, userId: String): Boolean {
        return repository.delete(id, userId)
    }

    suspend fun bulkCreate(requests: List<SendRequest>): List<Notification> {
        val notifications = requests.map { req ->
            Notification(
                userId = req.userId,
                type = req.type,
                title = req.title,
                message = req.message,
                tripId = req.tripId
            )
        }
        return repository.bulkCreate(notifications)
    }
}
