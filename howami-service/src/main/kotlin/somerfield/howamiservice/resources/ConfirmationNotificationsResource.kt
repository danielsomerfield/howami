package somerfield.howamiservice.resources

import somerfield.howamiservice.domain.RegistrationConfirmationService
import somerfield.howamiservice.wire.CommandResponseHeaderWireType
import somerfield.howamiservice.wire.CommandResponseWireType
import somerfield.howamiservice.wire.RegistrationConfirmationWireAdapter
import somerfield.resources.RequestIdSource
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/v1/confirmation-notifications")
class ConfirmationNotificationsResource(
        private val requestIdSource: RequestIdSource,
        private val registrationConfirmationService: RegistrationConfirmationService
) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun get(): Response {
        return Response.ok(
                CommandResponseWireType(
                        header = CommandResponseHeaderWireType(requestIdSource.getOrCreate()),
                        body = RegistrationConfirmationWireAdapter.toWireType(registrationConfirmationService.getUnsentConfirmations())
                )
        ).build()
    }

}