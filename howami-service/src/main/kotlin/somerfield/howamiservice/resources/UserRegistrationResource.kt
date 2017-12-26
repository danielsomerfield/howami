package somerfield.howamiservice.resources

import somerfield.howamiservice.domain.*
import somerfield.howamiservice.wire.*
import somerfield.resources.WireOperations
import somerfield.resources.WireOperations.sendFailureResponse
import javax.validation.Valid
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/v1/user-registrations")
public class UserRegistrationResource(private val userRegistrationService: UserRegistrationService) {

    @POST()
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun register(
            @Valid command: CommandWireType<UserRegistrationWireTypes>,
            @HeaderParam("request-id") requestId: String
    ): Response {
        val registrationResponse = userRegistrationService.register(fromWireType(command.body))
        return when (registrationResponse) {
            is Result.Success -> {
                sendSuccessResponse(requestId, registrationResponse)
            }
            is Result.Failure -> sendFailureResponse(requestId, registrationResponse)
        }
    }


    private fun sendSuccessResponse(requestId: String, registration: Result.Success<UserRegistration>): Response {
        return Response.ok(
                CommandResponseWireType(
                        header = CommandResponseHeaderWireType(requestId),
                        body = toWireType(registration.response)
                )
        ).build()
    }

    private fun fromWireType(orderCommandWireType: UserRegistrationWireTypes): UserRegistrationCommand {
        return UserRegistrationCommand(
                orderCommandWireType.username,
                orderCommandWireType.password,
                orderCommandWireType.email
        )
    }

    private fun toWireType(userRegistration: UserRegistration): UserRegistrationResponseWireType {
        return UserRegistrationResponseWireType(userRegistration.userId)
    }

}