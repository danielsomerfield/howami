package somerfield.howamiservice.resources

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import somerfield.howamiservice.domain.*
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
                register(UserRegistrationCommand(username, password, emailAddress))
            } doReturn (Result.Success(UserRegistration(generatedUserId)))
        }
        val registerResponse = UserRegistrationResource(userRegistrationService, requestIdSource).register(
                CommandWireType(UserRegistrationWireTypes(username, password, emailAddress))
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

        val userRegistrationService: UserRegistrationService = mock {
            on {
                register(any())
            } doReturn (Result.Failure(UnknownError(errorCode, "error message")))
        }
        val registerResponse = UserRegistrationResource(userRegistrationService, requestIdSource).register(
                CommandWireType(UserRegistrationWireTypes("username2", "password1", "555-123-1234"))
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

