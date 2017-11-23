package somerfield.howamiservice

import org.apache.commons.lang3.RandomStringUtils.randomNumeric
import org.hamcrest.CoreMatchers.`is`
import org.json.JSONObject
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
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
        val phoneNumber = user.phoneNumber

        val registration = user.register(
                username = UUID.randomUUID().toString(),
                password = UUID.randomUUID().toString()
        )

//        waitFor({ responseOf { user.receivedSMS() } }).toExist().then { sms ->
//            user.confirm(sms.passCode)
//        }
//
//        waitFor({ responseOf { UserAccount.registrationConfirmed(registration) } }).toExist()
    }

}

//sealed class Envelope<out T> {
//    abstract val header: Header
//}

data class Header(val requestId: String, val status: Int)
//data class EnvelopeWithData<out T>(override val header: Header, val data: T) : Envelope<T>()
data class UserRegistration(val userId: String)

data class SMSMessage(val phoneNumber: String, val passCode: String)

class User() {

    val phoneNumber = generatePhoneNumber()

    fun register(username: String, password: String): UserRegistration {
        return UserRegistrationService.registerUser(username, password, phoneNumber)
    }

    fun receivedSMS(): SMSMessage {
        TODO()
    }

    fun confirm(passCode: String) {}

    companion object {
        fun generatePhoneNumber() = "555-${randomNumeric(3)}-${randomNumeric(4)}"
    }

}


object UserAccount {
    fun registrationConfirmed(registration: UserRegistration): UserRegistration {
        TODO("NYI")
    }

}

object UserRegistrationService : HealthCheckService {

    private fun getServiceHost() = System.getProperty("HOWAMI_SERVICE_BASE_URL", "http://howami-service")

    override fun healthEndpoint(): URI {
        return URI.create("${getServiceHost()}:${getHealthPort()}/healthcheck")
    }

    private fun getHealthPort(): Int {
        return 8081
    }

    private fun getServicePort(): Int {
        return 8080
    }

    fun registerUser(username: String, password: String, phoneNumber: String): UserRegistration {
        val message = JSONObject()
                .put("header", JSONObject()
                        .put("request-id", UUID.randomUUID().toString())
                )
                .put("body", JSONObject()
                        .put("username", username)
                        .put("password", password)
                        .put("phone-number", phoneNumber)
                )

        val response = HTTP.post(
                to = URI.create("${getServiceHost()}:${getServicePort()}/api/v1/user-registrations"),
                contentType = "application/json",
                content = message.toString()
        )
        assertThat(response.status, `is`(200))
        return UserRegistration(response.json.getJSONObject("body").getString("user-id"))
    }

}
