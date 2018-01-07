package somerfield.howami.commsservice.domain

class NotificationQueueService {

    fun getPendingNotifications(): List<PendingNotification> {
        TODO()
    }

    fun confirmingNotificationSent(userId: String) {
        TODO()
    }
}

data class PendingNotification(val userId: String, val email: String, val confirmationCode: String)