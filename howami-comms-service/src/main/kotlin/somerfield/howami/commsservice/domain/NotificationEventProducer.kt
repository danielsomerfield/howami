package somerfield.howami.commsservice.domain

import org.apache.kafka.clients.producer.Producer

class NotificationEventProducer(
        private val kafkaProducer: Producer<Unit, ByteArray>

) {
    fun send(notificationSentEvent: NotificationSentEvent) {
//        TODO("NYI")
    }

    fun send(notificationSentEvent: NotificationSendFailedEvent) {
//        TODO("NYI")
    }
}

data class NotificationSentEvent(
        val userId: String
)

data class NotificationSendFailedEvent(
        val userId: String,
        val errorMessage: String
)
