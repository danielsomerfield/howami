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
import javax.ws.rs.core.Response

class RegistrationConfirmationResourceTest {

    @Test
    fun testNoResults() {
        val requestId = UUID.randomUUID().toString()

        val registrationConfirmationService = mock<RegistrationConfirmationService>() {
            on {
                getAllConfirmations()
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
                getAllConfirmations()
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

        assertThat(toWireType(confirmationResource.getAll(requestId)), `is`(expectedResponse))
    }

    @Suppress("UNCHECKED_CAST")
    private fun toWireType(response: Response) =
            response.entity as CommandResponseWireType<List<RegistrationConfirmationWireType>>

    @Test
    fun testGetByStatus() {
        val requestId = UUID.randomUUID().toString()

        val registrationConfirmationService = mock<RegistrationConfirmationService>() {
            on {
                getConfirmations(status = ConfirmationStatus.SENT)
            } doReturn (Result.Success(listOf(
                    RegistrationConfirmation(
                            email = "sent-test@example.com",
                            userId = UUID.randomUUID().toString(),
                            confirmationCode = UUID.randomUUID().toString(),
                            createdDateTime = Date(),
                            confirmationStatus = ConfirmationStatus.SENT
                    )
            )))

            on {
                getConfirmations(status = ConfirmationStatus.CONFIRMED)
            } doReturn (Result.Success(listOf(
                    RegistrationConfirmation(
                            email = "confirmed-test@example.com",
                            userId = UUID.randomUUID().toString(),
                            confirmationCode = UUID.randomUUID().toString(),
                            createdDateTime = Date(),
                            confirmationStatus = ConfirmationStatus.CONFIRMED
                    )
            )))
        }


        val confirmationResource = RegistrationConfirmationResource(
                registrationConfirmationService = registrationConfirmationService
        )

        val sentConfirmations = toWireType(confirmationResource.getRegistrationConfirmations(
                status = "SENT",
                requestId = requestId
        ))
        assertThat(sentConfirmations.body.map { it.email }, `is`(listOf("sent-test@example.com")))

        val queuedConfirmations = toWireType(confirmationResource.getRegistrationConfirmations(
                status = "CONFIRMED",
                requestId = requestId
        ))
        assertThat(queuedConfirmations.body.map { it.email }, `is`(listOf("confirmed-test@example.com")))

    }

    //TODO: validate that non-existing statuses are not allowed
}