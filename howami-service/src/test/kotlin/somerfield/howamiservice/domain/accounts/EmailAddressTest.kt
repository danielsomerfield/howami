package somerfield.howamiservice.domain.accounts

import org.hamcrest.CustomMatcher
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test
import somerfield.howamiservice.domain.ErrorResult
import somerfield.howamiservice.domain.Result
import kotlin.reflect.KClass

class EmailAddressTest {

    @Test
    fun testParsesEmailAddresses() {
        assertEquals(Result.Success(EmailAddress("dsomerfield@example.com")), "dsomerfield@example.com".toEmailAddress())
    }

    @Test
    fun testInvalidEmailAddressParseFails() {
        assertInvalidEmail("dsome rfield@example.com")
        assertInvalidEmail("dsomexample.com")
        assertInvalidEmail("dsomexample@")
    }

    private fun assertInvalidEmail(invalidEmail: String) {
        val maybeEmail = invalidEmail.toEmailAddress()
        assertThat(maybeEmail, failedWith(ParseFailure::class))
    }

    private fun failedWith(kClass: KClass<out ErrorResult>): Matcher<in Result<EmailAddress, ErrorResult>> {
        return object : CustomMatcher<Result<EmailAddress, ErrorResult>>("Failure of type $kClass"),
                Matcher<Result<EmailAddress, ErrorResult>> {

            override fun matches(item: Any?): Boolean {
                return when (item) {
                    is Result<*, *> -> checkResultType(item, kClass)
                    else -> false
                }
            }

            private fun checkResultType(item: Result<*, *>, kClass: KClass<out ErrorResult>): Boolean {
                return when (item) {
                    is Result.Failure<*> -> kClass.isInstance(item.errorValue)
                    else -> false
                }
            }


        }
    }
}