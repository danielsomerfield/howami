package somerfield.resources

import somerfield.howamiservice.domain.Result
import somerfield.howamiservice.domain.ServiceError
import somerfield.howamiservice.wire.CommandResponseHeaderWireType
import somerfield.howamiservice.wire.ErrorResponseWireType
import javax.ws.rs.core.Response

object WireOperations {
    fun sendFailureResponse(requestId: String, registrationResponse: Result.Failure<ServiceError>): Response {
        return Response.status(400).entity(
                ErrorResponseWireType(
                        header = CommandResponseHeaderWireType(requestId),
                        errorCode = registrationResponse.errorValue.errorCode(),
                        errorMessage = registrationResponse.errorValue.message()

                )
        ).build()
    }

}