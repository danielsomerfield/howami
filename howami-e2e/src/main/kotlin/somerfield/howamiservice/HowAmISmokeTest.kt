package somerfield.howamiservice

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import somerfield.howamiservice.domain.CommsServiceClient
import somerfield.howamiservice.domain.LoginResult
import somerfield.howamiservice.domain.LoginResult.*
import somerfield.howamiservice.domain.User
import somerfield.howamiservice.domain.UserServicesClient
import somerfield.testing.Async
import somerfield.testing.Async.responseOf
import somerfield.testing.Async.responseOfOptional
import somerfield.testing.Async.waitFor
import somerfield.testing.Async.waitForData
import somerfield.testing.Health
import somerfield.testing.Healthy
import somerfield.testing.Matchers.healthy
import somerfield.testing.toBe

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

    @Test(timeout = 30000)
    fun testUserRegistration() {
        assertThat(user.login(), `is`(FAILURE))
        user.register()

        //TODO: enable this once the confirmation messaging service is implemented
        waitForData(responseOfOptional { user.receiveConfirmationRequest() }).then { it ->
            assertThat(user.login(), `is`(FAILURE))
            println("*************************")
            println("********* $it ***********")
            println("*************************")
//            user.confirm(it)
//            assertThat(user.login(), `is`(SUCCESS))
        }
    }

}