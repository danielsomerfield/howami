package somerfield.howami.commsservice.domain

import somerfield.http.HttpClient

class NotificationQueueService(private val httpClient: HttpClient = HttpClient()) {

    fun getPendingNotifications(): List<PendingNotification> {
        TODO()
    }

    fun confirmingNotificationSent(userId: String) {
        TODO()
    }
}

data class PendingNotification(val userId: String, val email: String, val confirmationCode: String)