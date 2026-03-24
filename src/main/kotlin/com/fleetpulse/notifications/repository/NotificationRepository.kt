package com.fleetpulse.notifications.repository

import com.fleetpulse.notifications.models.Notification
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class NotificationRepository(private val collection: CoroutineCollection<Notification>) {

    suspend fun create(notification: Notification): Notification {
        collection.insertOne(notification)
        return notification
    }

    suspend fun bulkCreate(notifications: List<Notification>): List<Notification> {
        if (notifications.isNotEmpty()) {
            collection.insertMany(notifications)
        }
        return notifications
    }

    suspend fun findByUserId(userId: String, skip: Int = 0, limit: Int = 20): List<Notification> {
        return collection.find(Notification::userId eq userId)
            .skip(skip)
            .limit(limit)
            .descendingSort(Notification::createdAt)
            .toList()
    }

    suspend fun countByUserId(userId: String): Long {
        return collection.countDocuments(Notification::userId eq userId)
    }

    suspend fun getUnreadCount(userId: String): Long {
        return collection.countDocuments(
            org.litote.kmongo.and(
                Notification::userId eq userId,
                Notification::read eq false
            )
        )
    }

    suspend fun markAsRead(id: String, userId: String): Boolean {
        val result = collection.updateOne(
            org.litote.kmongo.and(
                Notification::id eq ObjectId(id),
                Notification::userId eq userId
            ),
            setValue(Notification::read, true)
        )
        return result.modifiedCount > 0
    }

    suspend fun markAllAsRead(userId: String): Long {
        val result = collection.updateMany(
            org.litote.kmongo.and(
                Notification::userId eq userId,
                Notification::read eq false
            ),
            setValue(Notification::read, true)
        )
        return result.modifiedCount
    }

    suspend fun delete(id: String, userId: String): Boolean {
        val result = collection.deleteOne(
            org.litote.kmongo.and(
                Notification::id eq ObjectId(id),
                Notification::userId eq userId
            )
        )
        return result.deletedCount > 0
    }

    suspend fun findById(id: String): Notification? {
        return collection.findOneById(ObjectId(id))
    }
}
