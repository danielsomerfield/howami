package somerfield.howami.commsservice.domain

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import somerfield.howami.commsservice.wire.EventWireType
import somerfield.howami.commsservice.wire.NotificationFailedEventWireType
import somerfield.howami.commsservice.wire.NotificationSentEventWireType
import somerfield.howamiservice.wire.JSON

class NotificationEventProducer(
        private val kafkaProducer: Producer<Unit, ByteArray>

) {
    private val notificationSentTopic = "registration-notification-sent-event"
    private val notificationFailedTopic = "registration-notification-failed-event"

    private val objectMapper = JSON.configureObjectMapper(ObjectMapper())

    fun send(notificationSentEvent: NotificationSentEvent) {
        val messageBytes = objectMapper.writeValueAsBytes(EventWireType(toWireType(notificationSentEvent)))
        kafkaProducer.send(ProducerRecord(notificationSentTopic, messageBytes))
    }

    fun send(notificationSendFailedEvent: NotificationSendFailedEvent) {
        val messageBytes = objectMapper.writeValueAsBytes(EventWireType(toWireType(notificationSendFailedEvent)))
        kafkaProducer.send(ProducerRecord(notificationFailedTopic, messageBytes))
    }

    private fun toWireType(event: NotificationSentEvent) = NotificationSentEventWireType(event.userId)
    private fun toWireType(event: NotificationSendFailedEvent) = NotificationFailedEventWireType(event.userId, event.errorMessage)
}

data class NotificationSentEvent(
        val userId: String
)

data class NotificationSendFailedEvent(
        val userId: String,
        val errorMessage: String
)
