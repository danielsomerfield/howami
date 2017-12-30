package somerfield.howamiservice.resources

import somerfield.howamiservice.domain.LoginService
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/v1/login")
class LoginResource(private val loginService: LoginService) {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun login(
            @FormParam("username") username: String,
            @FormParam("password") password: String
    ): Response {
        val response = loginService.login(username, password)
        return Response.status(401).build()
    }
}