package somerfield.howamiservice.resources

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import somerfield.howamiservice.domain.*
import somerfield.howamiservice.wire.*

class UserRegistrationResourceTest {

    @Test
    fun successfulRegistrationReturnsUserId() {
        val generatedUserId = "generated-id"
        val requestId = "1234"
        val username = "username1"
        val password = "password1"
        val phoneNumber = "555-123-1234"

        val userRegistrationService: UserRegistrationService = mock<UserRegistrationService> {
            on {
                register(UserRegistrationCommand(username, password, phoneNumber))
            } doReturn (Result.Success(UserRegistration(generatedUserId)))
        }
        val registerResponse = UserRegistrationResource(userRegistrationService).register(
                CommandWireType(UserRegistrationWireTypes(username, password, phoneNumber)), requestId
        )

        assertThat(registerResponse.status, `is`(200))
        val expectedResponse = CommandResponseWireType(
                header = CommandResponseHeaderWireType(requestId = requestId),
                body = UserRegistrationResponseWireType(generatedUserId)
        )

        @Suppress("UNCHECKED_CAST")
        assertThat(registerResponse.entity as CommandResponseWireType<UserRegistrationResponseWireType>, `is`(expectedResponse))
    }

    @Test
    fun failedRegistrationReturns400Error() {
        val errorCode = "ERR_CODE"
        val errorMessage = "error message"
        val requestId = "1234"

        val userRegistrationService: UserRegistrationService = mock<UserRegistrationService> {
            on {
                register(any())
            } doReturn (Result.Failure(UnknownError(errorCode, "error message")))
        }
        val registerResponse = UserRegistrationResource(userRegistrationService).register(
                CommandWireType(UserRegistrationWireTypes("username2", "password1", "555-123-1234")),
                requestId
        )

        assertThat(registerResponse.status, `is`(400))
        val expectedResponse = ErrorResponseWireType(
                header = CommandResponseHeaderWireType(requestId = requestId),
                errorCode = errorCode,
                errorMessage = errorMessage

        )

        @Suppress("UNCHECKED_CAST")
        assertThat(registerResponse.entity as ErrorResponseWireType, `is`(expectedResponse))
    }

}

