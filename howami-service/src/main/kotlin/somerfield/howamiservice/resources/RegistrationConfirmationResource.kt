package somerfield.howamiservice.resources

import somerfield.howamiservice.domain.ConfirmationStatus
import somerfield.howamiservice.domain.RegistrationConfirmation
import somerfield.howamiservice.domain.RegistrationConfirmationService
import somerfield.howamiservice.domain.Result
import somerfield.howamiservice.wire.CommandResponseHeaderWireType
import somerfield.howamiservice.wire.CommandResponseWireType
import somerfield.howamiservice.wire.RegistrationConfirmationWireType
import javax.ws.rs.GET
import javax.ws.rs.HeaderParam
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/v1/registration-confirmations")
class RegistrationConfirmationResource(
        private val registrationConfirmationService: RegistrationConfirmationService
) {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getAll(@HeaderParam("request-id") requestId: String): Response {
        val outstandingConfirmations =
                registrationConfirmationService.getOutstandingConfirmations()
        val header = CommandResponseHeaderWireType(requestId)

        return when(outstandingConfirmations) {
            is Result.Success -> sendSuccessResponse(header, outstandingConfirmations.response)
            else -> TODO()
        }

}

    private fun sendSuccessResponse(header: CommandResponseHeaderWireType, confirmations: List<RegistrationConfirmation>): Response {
        return Response.ok(
                CommandResponseWireType(
                        header = header,
                        body = toWireType(confirmations)
                )
        ).build()
    }

    private fun toWireType(outstandingConfirmations: List<RegistrationConfirmation>): List<RegistrationConfirmationWireType> {
        return outstandingConfirmations.map { RegistrationConfirmationWireType(
                it.email,
                it.userId,
                it.confirmationCode,
                it.createdDateTime,
                it.confirmationStatus.name
        ) }
    }
}