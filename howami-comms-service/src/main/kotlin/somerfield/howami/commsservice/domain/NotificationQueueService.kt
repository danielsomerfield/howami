package somerfield.howami.commsservice.domain

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import somerfield.howami.commsservice.wire.PendingNotificationWireType
import somerfield.howamiservice.wire.CommandResponseWireType
import somerfield.http.HttpClient
import java.io.InputStream
import java.net.URI

class NotificationQueueService(
        private val httpClient: HttpClient = HttpClient(),
        private val baseURI: String
) {

    private val servicePath = "api/v1/confirmation-notifications"
    private val objectMapper = ObjectMapper().registerKotlinModule()

    fun getPendingNotifications(): List<PendingNotification> {
        val to = URI.create("$baseURI/$servicePath")
        println(to)
        return httpClient.get(
                to
        ).entityStream.map { input ->
            fromWireType(input)
        }.orElse(emptyList())
    }

    private val typeReference = object : TypeReference<CommandResponseWireType<List<PendingNotificationWireType>>>() {}

    private fun fromWireType(input: InputStream): List<PendingNotification> {
        val (header, body) = objectMapper.readValue(input, typeReference) as CommandResponseWireType<List<PendingNotificationWireType>>
        return body.map { fromWireType(it) }
    }

    private fun fromWireType(wireType: PendingNotificationWireType): PendingNotification {
        return PendingNotification(
                userId = wireType.userId,
                email = wireType.email,
                confirmationCode = wireType.confirmationCode
        )
    }

    fun confirmingNotificationSent(userId: String) {
        TODO()
    }
}

data class PendingNotification(val userId: String, val email: String, val confirmationCode: String)
