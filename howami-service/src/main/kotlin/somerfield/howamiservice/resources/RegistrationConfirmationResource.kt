package somerfield.howamiservice.resources

import somerfield.howamiservice.domain.ConfirmationStatus
import somerfield.howamiservice.domain.RegistrationConfirmation
import somerfield.howamiservice.domain.RegistrationConfirmationService
import somerfield.howamiservice.wire.CommandResponseHeaderWireType
import somerfield.howamiservice.wire.CommandResponseWireType
import somerfield.howamiservice.wire.RegistrationConfirmationWireType
import somerfield.resources.RequestIdSource
import java.time.format.DateTimeFormatter
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Request
import javax.ws.rs.core.Response

@Path("/api/v1/registration-confirmations")
class RegistrationConfirmationResource(
        private val registrationConfirmationService: RegistrationConfirmationService,
        private val requestIdSource: RequestIdSource
) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getAllRegistrationConfirmations(): Response {
        val result =
                registrationConfirmationService.getAllConfirmations()

        return sendSuccessResponse(
                requestIdSource.getOrCreate(),
                result
        )
    }

    private fun sendSuccessResponse(requestId: String, confirmations: List<RegistrationConfirmation>): Response {
        return Response.ok(
                CommandResponseWireType(
                        header = CommandResponseHeaderWireType(requestId),
                        body = toWireType(confirmations)
                )
        ).build()
    }

    private fun toWireType(outstandingConfirmations: List<RegistrationConfirmation>): List<RegistrationConfirmationWireType> {
        return outstandingConfirmations.map {
            RegistrationConfirmationWireType(
                    it.email,
                    it.userId,
                    it.confirmationCode,
                    DateTimeFormatter.ISO_INSTANT.format(it.createdDateTime),
                    it.confirmationStatus.name
            )
        }
    }
}