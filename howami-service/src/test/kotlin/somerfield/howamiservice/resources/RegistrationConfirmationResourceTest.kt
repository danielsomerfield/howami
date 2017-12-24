package somerfield.howamiservice.resources

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test
import somerfield.howamiservice.domain.Result
import somerfield.howamiservice.wire.CommandResponseHeaderWireType
import somerfield.howamiservice.wire.CommandResponseWireType
import java.util.*

class RegistrationConfirmationResourceTest {

    @Test
    fun testNoResults() {
        val requestId = UUID.randomUUID().toString()

        val registrationConfirmationService = mock<RegistrationConfirmationService>() {
            on {
                getOutstandingConfirmations()
            } doReturn (Result.Success(emptyList<RegistrationConfirmation>()))
        }

        val confirmationResource = RegistrationConfirmationResource(
                registrationConfirmationService = registrationConfirmationService
        )

        val expectedResponse = CommandResponseWireType(
                header = CommandResponseHeaderWireType(requestId = requestId),
                body = emptyList<RegistrationConfirmationWireType>())

        @Suppress("UNCHECKED_CAST")
        assertThat(confirmationResource.getAll(requestId).entity as CommandResponseWireType<List<RegistrationConfirmationWireType>>, `is`(expectedResponse))
    }
}

class RegistrationConfirmationWireType