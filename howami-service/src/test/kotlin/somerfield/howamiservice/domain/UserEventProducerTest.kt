package somerfield.howamiservice.domain

import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.json.JSONObject
import org.junit.Test
import java.util.*

class UserEventProducerTest {

    private val kafkaProducer = mock<Producer<Unit, ByteArray>> {

    }
    private val producer = UserEventProducer(kafkaProducer)

    @Test
    fun userRegisteredEvent() {
        val userId = UUID.randomUUID().toString()
        val emailAddress = "$userId@example.com"
        val confirmationCode = UUID.randomUUID().toString()

        val expectedMessage = """
            {
                "body": {
                    "user-id":"$userId",
                    "email-address":"$emailAddress",
                    "confirmation-code":"$confirmationCode"
                }
            }
        """.trimIndent()


        producer.userRegistered(UserRegistrationEvent(
                userId = userId,
                emailAddress = emailAddress,
                confirmationCode = confirmationCode
        ));

        verify(kafkaProducer).send(argThat { matches(jsonMessage = expectedMessage, topicName = "user-registration-event") })
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
}