package somerfield.howamiservice.domain

import org.apache.commons.lang3.RandomStringUtils
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

    fun register(): UserRegistration {
        return UserServicesClient.registerUser(username, password, email)
    }

    fun receiveConfirmationRequest(): Optional<RegistrationConfirmation> {
        //TODO: read from kafka topic
        return Optional.empty()
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
}

enum class ConfirmationStatus {
    SENT,
    CONFIRMED,
    QUEUED
}

data class RegistrationConfirmation(
        val userId: String,
        val confirmationCode: String,
        val createdDateTime: Date,
        val confirmationStatus: ConfirmationStatus
)