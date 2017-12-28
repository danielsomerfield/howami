package somerfield.howamiservice

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import somerfield.howamiservice.domain.User
import somerfield.howamiservice.domain.UserRegistrationService
import somerfield.testing.*
import somerfield.testing.Async.responseOf
import somerfield.testing.Async.responseOfOptional
import somerfield.testing.Async.waitFor
import somerfield.testing.Async.waitForData
import somerfield.testing.Matchers.healthy

class HowAmISmokeTest {

    private val user = User()

    @Before
    fun setup() {
        waitFor(responseOf { Health.check(UserRegistrationService) }, toBe(Healthy))
    }

    @Test(timeout = 30000)
    fun testServiceHealth() {
        assertThat(Health.check(UserRegistrationService), `is`(healthy()))
    }

    @Test(timeout = 30000)
    fun testUserRegistration() {
        val registration = user.register()

        waitForData(responseOfOptional { user.receiveConfirmationRequest() }).then { it ->
            user.confirm(it)
        }
//        waitFor({ responseOf { UserAccount.registrationConfirmed(registration) } }).toExist()
    }

}