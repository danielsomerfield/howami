package somerfield.howamiservice.domain

import org.apache.commons.lang3.RandomStringUtils
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.joda.time.format.ISODateTimeFormat
import org.json.JSONObject
import org.junit.Assert
import org.junit.Assert.assertThat
import somerfield.howamiservice.domain.LoginResult.*
import somerfield.testing.HTTP
import somerfield.testing.HealthCheckService
import java.net.URI
import java.net.URLEncoder
import java.util.*

class User {

    private val randomNumeric = RandomStringUtils.randomNumeric(10)
    private val username = "user-$randomNumeric"
    private val password = "password-$randomNumeric"
    private val email = "email-$randomNumeric@example.com"
    private val kafkaBootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")

    private val consumer = KafkaConsumer<Unit, ByteArray>(mapOf(
            "bootstrap.servers" to kafkaBootstrapServers,
            "group.id" to "howami-e2e-user",
            "enable.auto.commit" to "true",
            "key.deserializer" to "org.apache.kafka.common.serialization.ByteArrayDeserializer",
            "value.deserializer" to "org.apache.kafka.common.serialization.ByteArrayDeserializer"
    ))

    init {
        consumer.subscribe(mutableListOf("registration-notification-sent-event"))
    }

    fun register(): UserRegistration {
        return UserServicesClient.registerUser(username, password, email)
    }

    fun receiveConfirmationRequest(): Optional<RegistrationConfirmation> {
        return consumer.poll(100)
                .map { record -> record.value() }
                .map { bytes -> JSONObject(String(bytes, Charsets.UTF_8)) }
                .firstOrNull()
                .toOptional()
                .flatMap { notificationSentEventJSONToUserId(it) }
                .flatMap { id -> UserServicesClient.getConfirmations(id) }
    }

    companion object {
        fun notificationSentEventJSONToUserId(jsonObject: JSONObject): Optional<String> {
            return try {
                val body = jsonObject.getJSONObject("body")
                Optional.of(body.getString("user-id"))
            } catch (e: Exception) {
                println("Failed to parse $jsonObject")
                e.printStackTrace()
                Optional.empty()
            }
        }
    }

    fun confirm(confirmation: RegistrationConfirmation) {
        UserServicesClient.confirmRegistration(confirmation.userId, confirmation.confirmationCode)
    }

    fun login(): LoginResult {
        return UserServicesClient.login(username, password)
    }

}

enum class LoginResult {
    FAILURE,
    SUCCESS,
    ERROR
}

data class UserRegistration(val userId: String)

object UserServicesClient : HealthCheckService {

    private fun getServiceHost() = System.getenv().getOrDefault("HOWAMI_SERVICE_BASE_URL", "http://localhost")

    override fun healthEndpoint(): URI {
        return URI.create("${getServiceHost()}:${getHealthPort()}/healthcheck")
    }

    private fun getHealthPort(): Int {
        return 8081
    }

    private fun getServicePort(): Int {
        return 8080
    }

    fun registerUser(username: String, password: String, email: String): UserRegistration {
        val requestId = UUID.randomUUID().toString()
        val message = JSONObject()
                .put("body", JSONObject()
                        .put("username", username)
                        .put("password", password)
                        .put("email", email)
                )

        val response = HTTP.post(
                to = URI.create("${getServiceHost()}:${getServicePort()}/api/v1/user-registrations"),
                contentType = "application/json",
                content = message.toString(),
                headers = mapOf("request-id" to requestId)

        )
        Assert.assertThat(response.status, CoreMatchers.`is`(200))
        return UserRegistration(response.json.getJSONObject("body").getString("user-id"))
    }

    private fun parseConfirmationRequest(json: JSONObject): List<RegistrationConfirmation> {
        return json.getJSONArray("body").map {
            val confReqJSON = it as JSONObject
            RegistrationConfirmation(
                    confReqJSON.getString("user-id"),
                    confReqJSON.getString("confirmation-code"),
                    ISODateTimeFormat.dateTimeParser().parseDateTime(confReqJSON.getString("created-datetime")).toDate(),
                    ConfirmationStatus.valueOf(confReqJSON.getString("confirmation-status"))
            )
        }
    }

    fun confirmRegistration(userId: String, confirmationCode: String) {
        val response = HTTP.post(
                to = URI.create("${getServiceHost()}:${getServicePort()}/api/v1/registration-confirmations"),
                contentType = "application/x-www-form-urlencoded",
                content = "userId=${encode(userId)}&confirmationCode=${encode(confirmationCode)}"
        )

        assertThat(response.status, `is`(201))
    }

    fun login(username: String, password: String): LoginResult {
        val response = HTTP.post(
                to = URI.create("${getServiceHost()}:${getServicePort()}/api/v1/login"),
                content = "username=${encode(username)}&password=${encode(password)}",
                contentType = "application/x-www-form-urlencoded"
        )
        return when (response.status) {
            200 -> SUCCESS
            in (400..499) -> FAILURE
            else -> ERROR
        }
    }

    private fun encode(paramValue: String) = URLEncoder.encode(paramValue, "UTF-8")

    fun getConfirmations(userId: String): Optional<RegistrationConfirmation> {
        val response = HTTP.get(
                to = URI.create("${getServiceHost()}:${getServicePort()}/api/v1/registration-confirmations"),
                headers = mapOf("Authorization" to "changeme")
        )
        return when (response.status) {
            404 -> Optional.empty()
            200 -> parseConfirmationRequest(response.json).filter {
                it.userId == userId
            }.first().toOptional()
            else -> throw Exception("Unexpected status code ${response.status}")
        }
    }
}

enum class ConfirmationStatus {
    CONFIRMED,
    UNCONFIRMED
}

data class RegistrationConfirmation(
        val userId: String,
        val confirmationCode: String,
        val createdDateTime: Date,
        val confirmationStatus: ConfirmationStatus
)

fun <T> T?.toOptional() = Optional.ofNullable<T>(this)