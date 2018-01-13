package somerfield.howamiservice.resources

import io.swagger.annotations.Api
import somerfield.howamiservice.domain.ConfirmationResult
import somerfield.howamiservice.domain.ConfirmationResult.*
import somerfield.howamiservice.domain.RegistrationConfirmation
import somerfield.howamiservice.domain.RegistrationConfirmationService
import somerfield.howamiservice.wire.*
import somerfield.resources.RequestIdSource
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/v1/registration-confirmations")
@Api()
class RegistrationConfirmationResource(
        private val registrationConfirmationService: RegistrationConfirmationService,
        private val requestIdSource: RequestIdSource
) {

    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getAllRegistrationConfirmations(): Response {
        val result =
                registrationConfirmationService.getAllConfirmations()

        return sendSuccessResponse(
                requestIdSource.getOrCreate(),
                result
        )
    }*/

    @POST
    fun confirmRegistration(
            @FormParam("userId")
            userId: String,
            @FormParam("confirmationCode")
            confirmationCode: String
    ): Response {
        val confirmationResult = registrationConfirmationService.confirm(userId, confirmationCode)
        return when (confirmationResult) {
            CONFIRMED -> sendWithMessage(201, confirmationResult, "Account confirmed")
            EXPIRED -> sendWithMessage(401, confirmationResult, "The confirmation has expired. Please request a new confirmation code.")
            INVALID -> sendWithMessage(401, confirmationResult, "The confirmation code is invalid.")
        }.build()
    }

    private fun sendWithMessage(statusCode: Int, confirmationResult: ConfirmationResult, message: String): Response.ResponseBuilder {
        return Response.status(statusCode).entity(CommandResponseWireType(
                header = CommandResponseHeaderWireType(requestIdSource.getOrCreate()),
                body = ConfirmationResponseWireType(
                        result = confirmationResult.name,
                        message = message
                )
        )).type(MediaType.APPLICATION_JSON_TYPE)
    }

    private fun sendSuccessResponse(requestId: String, confirmations: List<RegistrationConfirmation>): Response {
        return Response.ok(
                CommandResponseWireType(
                        header = CommandResponseHeaderWireType(requestId),
                        body = RegistrationConfirmationWireAdapter.toWireType(confirmations)
                )
        ).build()
    }
}

