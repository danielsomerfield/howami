package somerfield.howami.commsservice.domain

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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
        ).entityStream.map {input ->
            fromWireType(input)
        }.orElse(emptyList())
    }

    private val typeReference = object : TypeReference<List<PendingNotification>>() {

    }

    private fun fromWireType(input: InputStream): List<PendingNotification> {
        TODO()
    }

    fun confirmingNotificationSent(userId: String) {
        TODO()
    }
}

//TODO: need wire types with entire message shape
data class PendingNotification(val userId: String, val email: String, val confirmationCode: String)