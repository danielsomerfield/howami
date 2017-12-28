package somerfield.howamiservice.domain

import org.apache.commons.lang3.RandomStringUtils
import org.hamcrest.CoreMatchers
import org.joda.time.format.ISODateTimeFormat
import org.json.JSONObject
import org.junit.Assert
import somerfield.testing.HTTP
import somerfield.testing.HealthCheckService
import java.net.URI
import java.util.*

class User {

    private val randomNumeric = RandomStringUtils.randomNumeric(10)
    private val username = "user-$randomNumeric"
    private val password = "password-$randomNumeric"
    private val email = "email-$randomNumeric@example.com"

    fun register(): UserRegistration {
        return UserRegistrationService.registerUser(username, password, email)
    }

    fun receiveConfirmationRequest(): Optional<RegistrationConfirmation> {
        val requests = UserRegistrationService.getRegistrationConfirmation(email = email)
        return Optional.ofNullable(requests.sortedBy { request -> request.createdDateTime }.lastOrNull())
    }

    fun confirm(confirmation: RegistrationConfirmation) {
        println("Confirming pass code ${confirmation.confirmationCode}")
    }

}

data class UserRegistration(val userId: String)
object UserRegistrationService : HealthCheckService {

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

    fun getRegistrationConfirmation(email: String): Set<RegistrationConfirmation> {
        val response = HTTP.get(
                to = URI.create("${getServiceHost()}:${getServicePort()}/api/v1/registration-confirmations"),
                headers = mapOf("Authorization" to "changeme")
        )
        return when (response.status) {
            404 -> emptySet()
            200 -> parseConfirmationRequest(response.json).filter { it.email == email }.toSet()
            else -> throw Exception("Unexpected status code ${response.status}")
        }
    }

    private fun parseConfirmationRequest(json: JSONObject): List<RegistrationConfirmation> {
        return json.getJSONArray("body").map {
            val confReqJSON = it as JSONObject
            RegistrationConfirmation(
                    confReqJSON.getString("email"),
                    confReqJSON.getString("user-id"),
                    confReqJSON.getString("confirmation-code"),
                    ISODateTimeFormat.dateTimeParser().parseDateTime(confReqJSON.getString("created-datetime")).toDate(),
                    ConfirmationStatus.valueOf(confReqJSON.getString("confirmation-status"))
            )
        }
    }
}

enum class ConfirmationStatus {
    SENT,
    CONFIRMED,
    QUEUED
}

data class RegistrationConfirmation(
        val email: String,
        val userId: String,
        val confirmationCode: String,
        val createdDateTime: Date,
        val confirmationStatus: ConfirmationStatus
)