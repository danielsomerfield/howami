package somerfield.howamiservice.resources

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import somerfield.howamiservice.domain.*
import somerfield.howamiservice.domain.Result.Failure
import somerfield.howamiservice.domain.accounts.UserRegistration
import somerfield.howamiservice.domain.accounts.UserRegistrationCommand
import somerfield.howamiservice.domain.accounts.UserRegistrationService
import somerfield.howamiservice.domain.accounts.toEmailAddress
import somerfield.howamiservice.wire.*
import somerfield.resources.RequestIdSource

class UserRegistrationResourceTest {

    private val requestId = "1234"

    private val requestIdSource = mock<RequestIdSource> {
        on {
            getOrCreate()
        } doReturn requestId
    }

    @Test
    fun successfulRegistrationReturnsUserId() {
        val generatedUserId = "generated-id"
        val username = "username1"
        val password = "password1"
        val emailAddress = "foo@example.com"

        val userRegistrationService: UserRegistrationService = mock {
            on {
                register(UserRegistrationCommand(username, password, emailAddress.toEmailAddress().getUnsafe()))
            } doReturn (Result.Success(UserRegistration(generatedUserId)))
        }
        val registerResponse = UserRegistrationResource(userRegistrationService, requestIdSource).register(
                CommandWireType(UserRegistrationWireType(username, password, emailAddress))
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
        val errorMessage = "error message"
        val requestId = "1234"

        val userRegistrationService: UserRegistrationService = mock {
            on {
                register(any())
            } doReturn (Failure(BasicErrorResult(ErrorCode.UNKNOWN, "error message")))
        }
        val registerResponse = UserRegistrationResource(userRegistrationService, requestIdSource).register(
                CommandWireType(UserRegistrationWireType("username2", "password1", "test@example.com"))
        )

        assertThat(registerResponse.status, `is`(400))
        val expectedResponse = ErrorResponseWireType(
                header = CommandResponseHeaderWireType(requestId = requestId),
                errorCode = ErrorCode.UNKNOWN.name,
                errorMessage = errorMessage

        )

        @Suppress("UNCHECKED_CAST")
        assertThat(registerResponse.entity as ErrorResponseWireType, `is`(expectedResponse))
    }

}

