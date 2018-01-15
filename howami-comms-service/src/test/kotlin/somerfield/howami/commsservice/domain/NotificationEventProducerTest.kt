package somerfield.howami.commsservice.domain

import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.json.JSONObject
import org.junit.Test
import java.util.*

class NotificationEventProducerTest {

    private val userId = UUID.randomUUID().toString()

    private val kafkaProducer = mock<Producer<Unit, ByteArray>>() {}

    private val notificationEventProducer = NotificationEventProducer(
            kafkaProducer = kafkaProducer
    )

    private val expectedNotificationSentMessage = """
            {
                "body": {
                    "user-id":"$userId"
                }
            }
        """.trimIndent()

    private val expectedNotificationFailedMessage = """
            {
                "body": {
                    "user-id":"$userId",
                    "error-message":"send failed"
                }
            }
        """.trimIndent()

    @Test
    fun sendNotificationSentEvent() {
        notificationEventProducer.send(NotificationSentEvent(userId))
        verify(kafkaProducer).send(argThat { matches(jsonMessage = expectedNotificationSentMessage, topicName = "registration-notification-sent-event") })
    }

    @Test
    fun sendNotificationSendFailedEvent() {
        notificationEventProducer.send(NotificationSendFailedEvent(userId, "send failed"))
        verify(kafkaProducer).send(argThat { matches(
                jsonMessage = expectedNotificationFailedMessage, topicName = "registration-notification-failed-event") })
    }
}

private fun ProducerRecord<Unit, ByteArray>.matches(jsonMessage: String, topicName: String): Boolean {
    return jsonMessage.toJSONObject().toString() == this.value().toJSONObject().toString() && topicName == this.topic()
}

fun String.toJSONObject(): Optional<JSONObject> {
    return try {
        Optional.of(JSONObject(this))
    } catch (e: Exception) {
        Optional.empty()
    }
}

fun ByteArray.toJSONObject(): Optional<JSONObject> {
    return String(this, Charsets.UTF_8).toJSONObject()
}