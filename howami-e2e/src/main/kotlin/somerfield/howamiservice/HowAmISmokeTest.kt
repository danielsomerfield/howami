package somerfield.howamiservice

import org.apache.commons.lang3.RandomStringUtils.randomNumeric
import org.hamcrest.CoreMatchers.`is`
import org.joda.time.format.ISODateTimeFormat
import org.json.JSONObject
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import somerfield.testing.Async.optionalOfResponse
import somerfield.testing.Async.responseOf
import somerfield.testing.Async.waitFor
import somerfield.testing.HTTP
import somerfield.testing.Health
import somerfield.testing.HealthCheckService
import somerfield.testing.Healthy
import somerfield.testing.Matchers.healthy
import java.net.URI
import java.util.*

class HowAmISmokeTest {

    val user = User()

    @Before
    fun setup() {
        waitFor({ responseOf { Health.check(UserRegistrationService) } }).toBe(Healthy)
    }

    @Test(timeout = 5000)
    fun testServiceHealth() {
        assertThat(Health.check(UserRegistrationService), `is`(healthy()))
    }

    @Test(timeout = 5000)
    fun testUserRegistration() {
        val registration = user.register()

//        waitFor({ optionalOfResponse { user.receiveConfirmationRequest() } }).toExist().then { request ->
//            print(request)
//            user.confirm(request.passCode)
//        }
//        waitFor({ responseOf { UserAccount.registrationConfirmed(registration) } }).toExist()
    }

}

//sealed class Envelope<out T> {
//    abstract val header: Header
//}

data class Header(val requestId: String, val status: Int)
//data class EnvelopeWithData<out T>(override val header: Header, val data: T) : Envelope<T>()
data class UserRegistration(val userId: String)

class User() {

    private val randomNumeric = randomNumeric(10)
    private val username = "user-$randomNumeric"
    private val password = "password-$randomNumeric"
    private val email = "email-$randomNumeric@example.com"

    fun register(): UserRegistration {
        return UserRegistrationService.registerUser(username, password, email)
    }

    fun receiveConfirmationRequest(): Optional<ConfirmationRequest> {
        val requests = UserRegistrationService.getSentConfirmationRequestsForEmail(email);
        return Optional.ofNullable(requests.sortedBy { request -> request.createdDateTime }.lastOrNull())
    }

    fun confirm(passCode: String) {}

}

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
        val message = JSONObject()
                .put("header", JSONObject()
                        .put("request-id", UUID.randomUUID().toString())
                )
                .put("body", JSONObject()
                        .put("username", username)
                        .put("password", password)
                        .put("email", email)
                )

        val response = HTTP.post(
                to = URI.create("${getServiceHost()}:${getServicePort()}/api/v1/user-registrations"),
                contentType = "application/json",
                content = message.toString()
        )
        assertThat(response.status, `is`(200))
        return UserRegistration(response.json.getJSONObject("body").getString("user-id"))
    }

    fun getSentConfirmationRequestsForEmail(email: String): Set<ConfirmationRequest> {
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

    private fun parseConfirmationRequest(json: JSONObject): List<ConfirmationRequest> {
        return json.getJSONArray("body").map {
            val confReqJSON = it as JSONObject
            ConfirmationRequest(
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

data class ConfirmationRequest(val email: String, val userId: String, val confirmationCode: String, val createdDateTime: Date, val confirmationStatus: ConfirmationStatus)