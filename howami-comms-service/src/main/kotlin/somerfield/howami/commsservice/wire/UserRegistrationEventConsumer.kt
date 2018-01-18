package somerfield.howami.commsservice.wire

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import somerfield.howami.commsservice.domain.UserRegistrationEvent
import somerfield.howamiservice.wire.JSON
import java.io.Closeable
import java.util.*

typealias UserRegistrationCallback = (UserRegistrationEvent) -> Unit

class UserRegistrationEventConsumer(
        private val kafkaConsumer: Consumer<Unit, ByteArray>,
        private val callback: UserRegistrationCallback = {},
        private val configuration: Configuration = Configuration.default()
) : Closeable, Runnable {

    private val objectMapper = JSON.configureObjectMapper(ObjectMapper())
    private val typeRef = object : TypeReference<EventWireType<UserRegistrationEventWireType>>() {}
    private val logger = LoggerFactory.getLogger(javaClass)
    private var running = true

    private val subscribedTopics = listOf("user-registration-event")

    init {
        kafkaConsumer.subscribe(subscribedTopics)
    }

    override fun run() {
        while (running) {
            pollOnce()
        }
    }

    fun stop() {
        running = false
    }

    fun pollOnce() {
        kafkaConsumer.poll(configuration.pollTimeMillis)
                .map { message -> toWireType(message) }
                .filter { it.isPresent }
                .map { message -> message.get() }
                .forEach { callback(fromWireType(it)) }
    }

    private fun fromWireType(wire: UserRegistrationEventWireType) = UserRegistrationEvent(
            userId = wire.userId,
            emailAddress = wire.email,
            confirmationCode = wire.confirmationCode
    )


    private fun toWireType(record: ConsumerRecord<Unit, ByteArray>): Optional<UserRegistrationEventWireType> {
        return try {
            val event: EventWireType<UserRegistrationEventWireType> = objectMapper.readValue(record.value(), typeRef)
            Optional.ofNullable(event.body)
        } catch (e: Exception) {
            logger.warn("Bad user-registration-event record at offset ${record.offset()}, partition: ${record.partition()}, timestamp ${record.timestamp()}")
            Optional.empty()
        }
    }

    override fun close() {
        kafkaConsumer.close()
    }

    data class Configuration(
            val pollTimeMillis: Long = 1000 /*ms*/
    ) {
        companion object {
            fun default() = Configuration()
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class UserRegistrationEventWireType
    constructor(
            @JsonProperty("user-id")
            val userId: String,
            @JsonProperty("email")
            val email: String,
            @JsonProperty("confirmation-code")
            val confirmationCode: String
    )


}
