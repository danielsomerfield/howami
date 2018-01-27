package somerfield.howamiservice.domain.accounts

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import somerfield.howamiservice.wire.JSON

class UserEventProducer(
        private val kafkaProducer: Producer<Unit, ByteArray>
) {
    private val topicName = "user-registration-event"

    private val objectMapper = JSON.configureObjectMapper(ObjectMapper())

    fun userRegistered(userRegistrationEvent: UserRegistrationEvent) {
        val messageBytes = objectMapper.writeValueAsBytes(EventWireType(toWireType(userRegistrationEvent)))
        kafkaProducer.send(ProducerRecord(topicName, messageBytes))
    }

    private fun toWireType(event: UserRegistrationEvent) = UserRegistrationEventWireType(
            userId = event.userId,
            emailAddress = event.emailAddress.toString(),
            confirmationCode = event.confirmationCode
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class UserRegistrationEventWireType
    constructor(
            @JsonProperty("user-id")
            val userId: String,
            @JsonProperty("email-address")
            val emailAddress: String,
            @JsonProperty("confirmation-code")
            val confirmationCode: String
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventWireType<T>
constructor(
        @JsonProperty("body")
        val body: T
)

