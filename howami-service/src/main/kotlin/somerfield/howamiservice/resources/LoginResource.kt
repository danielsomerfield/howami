package somerfield.howamiservice.resources

import io.swagger.annotations.Api
import somerfield.howamiservice.domain.LoginResponse
import somerfield.howamiservice.domain.LoginService
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/v1/login")
@Api()
class LoginResource(private val loginService: LoginService) {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun login(
            @FormParam("username") username: String,
            @FormParam("password") password: String
    ): Response {
        val response = loginService.login(username, password)
        val status = if (response == LoginResponse.SUCCEEDED) 200 else 401
        return Response.status(status).build()
    }
}