package somerfield.howamiservice.resources

import somerfield.howamiservice.domain.Result
import somerfield.howamiservice.wire.CommandResponseHeaderWireType
import somerfield.howamiservice.wire.CommandResponseWireType
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
        val outstandingConfirmations = registrationConfirmationService.getOutstandingConfirmations()
        return Response.ok(CommandResponseWireType(
                CommandResponseHeaderWireType(requestId),
                body = emptyList<String>()
        )).build()
    }

}

class RegistrationConfirmationService {
    fun getOutstandingConfirmations(): Result<List<RegistrationConfirmation>, RegistrationConfirmationError> {
        TODO()
    }
}

data class RegistrationConfirmation(val passCode: String)
data class RegistrationConfirmationError(val errorCode: String, val message: String)