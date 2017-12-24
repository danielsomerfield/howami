package somerfield.howamiservice.resources

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test
import somerfield.howamiservice.domain.ConfirmationStatus
import somerfield.howamiservice.domain.RegistrationConfirmation
import somerfield.howamiservice.domain.RegistrationConfirmationService
import somerfield.howamiservice.domain.Result
import somerfield.howamiservice.wire.CommandResponseHeaderWireType
import somerfield.howamiservice.wire.CommandResponseWireType
import somerfield.howamiservice.wire.RegistrationConfirmationWireType
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

    @Test
    fun testOneExists() {
        val requestId = UUID.randomUUID().toString()
        val confirmationCode = UUID.randomUUID().toString()
        val email = "test@example.com"
        val userId = UUID.randomUUID().toString()
        val createdDateTime = Date()
        val confirmationStatus = ConfirmationStatus.QUEUED

        val registrationConfirmationService = mock<RegistrationConfirmationService>() {
            on {
                getOutstandingConfirmations()
            } doReturn (Result.Success(listOf(RegistrationConfirmation(
                    email = email,
                    userId = userId,
                    confirmationCode = confirmationCode,
                    createdDateTime = createdDateTime,
                    confirmationStatus = confirmationStatus
            ))))
        }

        val confirmationResource = RegistrationConfirmationResource(
                registrationConfirmationService = registrationConfirmationService
        )

        val expectedResponse = CommandResponseWireType(
                header = CommandResponseHeaderWireType(requestId = requestId),
                body = listOf(
                        RegistrationConfirmationWireType(
                                email = email,
                                userId = userId,
                                confirmationCode = confirmationCode,
                                createdDateTime = createdDateTime,
                                confirmationStatus = "QUEUED"
                        )
                ))

        @Suppress("UNCHECKED_CAST")
        assertThat(confirmationResource.getAll(requestId).entity as CommandResponseWireType<List<RegistrationConfirmationWireType>>, `is`(expectedResponse))
    }
}