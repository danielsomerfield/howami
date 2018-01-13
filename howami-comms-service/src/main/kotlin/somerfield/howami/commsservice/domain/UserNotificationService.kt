package somerfield.howami.commsservice.domain

class UserNotificationService {

    fun sendNotification(emailAddress: String, message: String): NotificationResponse {
        TODO("NYI")
    }
}

enum class NotificationStatus {
    SUCCESS,
    FAILED
}

data class NotificationResponse(
        val message: String,
        val result: NotificationStatus
)