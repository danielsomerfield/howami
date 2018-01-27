package somerfield.resources

import somerfield.howamiservice.domain.ErrorCode
import somerfield.howamiservice.domain.ErrorCodeErrorResult
import somerfield.howamiservice.domain.Result
import somerfield.howamiservice.domain.ErrorResult
import somerfield.howamiservice.wire.CommandResponseHeaderWireType
import somerfield.howamiservice.wire.ErrorResponseWireType
import javax.ws.rs.core.Response

object WireOperations {
    fun sendFailureResponse(requestId: String, registrationResponse: Result.Failure<ErrorResult>): Response {
        val errorCode = getErrorCodeForFailure(registrationResponse)

        return Response.status(400).entity(
                ErrorResponseWireType(
                        header = CommandResponseHeaderWireType(requestId),
                        errorCode = errorCode.name,
                        errorMessage = registrationResponse.errorValue.message

                )
        ).build()
    }

    private fun getErrorCodeForFailure(registrationResponse: Result.Failure<ErrorResult>): ErrorCode {
        return when (registrationResponse) {
            is ErrorCodeErrorResult -> registrationResponse.errorCode
            else -> ErrorCode.UNKNOWN
        }
    }

}