package somerfield.howamiservice.resources

import io.swagger.annotations.Api
import somerfield.howamiservice.domain.Result
import somerfield.howamiservice.domain.UserRegistration
import somerfield.howamiservice.domain.UserRegistrationCommand
import somerfield.howamiservice.domain.UserRegistrationService
import somerfield.howamiservice.wire.*
import somerfield.resources.RequestIdSource
import somerfield.resources.WireOperations.sendFailureResponse
import javax.validation.Valid
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/v1/user-registrations")
@Api()
class UserRegistrationResource(
        private val userRegistrationService: UserRegistrationService,
        private val requestIdSource: RequestIdSource

) {

    @POST()
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun register(
            @Valid command: CommandWireType<UserRegistrationWireTypes>
    ): Response {
        val registrationResponse = userRegistrationService.register(fromWireType(command.body))
        return when (registrationResponse) {
            is Result.Success -> {
                sendSuccessResponse(requestIdSource.getOrCreate(), registrationResponse)
            }
            is Result.Failure -> sendFailureResponse(requestIdSource.getOrCreate(), registrationResponse)
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