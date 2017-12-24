package somerfield.howamiservice.resources

import somerfield.howamiservice.domain.*
import somerfield.howamiservice.wire.*
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
        val header = CommandResponseHeaderWireType(requestId)
        return when (registrationResponse) {
            is Result.Success -> {
                sendSuccessResponse(header, registrationResponse)
            }
            is Result.Failure -> sendFailureResponse(header, registrationResponse)
        }
    }

    private fun sendFailureResponse(header: CommandResponseHeaderWireType, registrationResponse: Result.Failure<ServiceError>): Response {
        return Response.status(400).entity(
                ErrorResponseWireType(
                        header = header,
                        errorCode = registrationResponse.errorValue.errorCode(),
                        errorMessage = registrationResponse.errorValue.message()

                )
        ).build()
    }

    private fun sendSuccessResponse(header: CommandResponseHeaderWireType, registration: Result.Success<UserRegistration>): Response {
        return Response.ok(
                CommandResponseWireType(
                        header = header,
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