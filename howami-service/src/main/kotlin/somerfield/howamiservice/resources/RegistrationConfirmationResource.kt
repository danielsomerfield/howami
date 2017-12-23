package somerfield.howamiservice.resources

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/v1/registration-confirmations")
class RegistrationConfirmationResource {


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getAll(): Response {
        return Response.ok().build()
    }

}