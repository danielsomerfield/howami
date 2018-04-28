package somerfield.howamiservice

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import somerfield.howamiservice.domain.CommsServiceClient
import somerfield.howamiservice.domain.LoginResult.FAILURE
import somerfield.howamiservice.domain.LoginResult.SUCCESS
import somerfield.howamiservice.domain.User
import somerfield.howamiservice.domain.UserServicesClient
import somerfield.testing.Async.responseOf
import somerfield.testing.Async.responseOfOptional
import somerfield.testing.Async.waitFor
import somerfield.testing.Async.waitForData
import somerfield.testing.Health
import somerfield.testing.Healthy
import somerfield.testing.Matchers.healthy
import somerfield.testing.toBe
import java.time.Instant

class HowAmISmokeTest {

    private val user = User()

    @Before
    fun setup() {
        waitFor(responseOf { Health.check(UserServicesClient) }, toBe(Healthy))
        waitFor(responseOf { Health.check(CommsServiceClient) }, toBe(Healthy))
    }

    @Test(timeout = 30000)
    fun testServiceHealth() {
        assertThat(Health.check(UserServicesClient), `is`(healthy()))
    }

    @Test(timeout = 35000)
    fun testUserRegistration() {
        println("Starting user registration test with user ${user} at ${Instant.now()}")
        assertThat(user.login(), `is`(FAILURE))
        user.register()

        waitForData(responseOfOptional { user.receiveConfirmationRequest() }).then { it ->
            assertThat(user.login(), `is`(FAILURE))
            user.confirm(it)
            assertThat(user.login(), `is`(SUCCESS))
        }
    }

}