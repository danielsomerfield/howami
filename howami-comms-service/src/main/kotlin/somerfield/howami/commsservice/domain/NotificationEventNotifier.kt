package somerfield.howami.commsservice.domain

class NotificationEventNotifier {
    fun send(notificationSentEvent: NotificationSentEvent) {
        TODO("NYI")
    }

    fun send(notificationSentEvent: NotificationSendFailedEvent) {
        TODO("NYI")
    }
}

data class NotificationSentEvent(
        val userId: String
)

data class NotificationSendFailedEvent(
        val userId: String,
        val errorMessage: String
)
