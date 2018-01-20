package somerfield.howamiservice.domain

sealed class Result<out OT, out OU> {
    data class Success<out T>(val response: T) : Result<T, Nothing>()
    data class Failure<out U : ServiceError>(val errorValue: U) : Result<Nothing, U>()
}

interface ServiceError {
    fun message(): String
    fun errorCode(): ErrorCode
}

data class BasicError(private val errorCode: ErrorCode, private val message: String) : ServiceError {
    override fun message(): String {
        return message
    }

    override fun errorCode(): ErrorCode {
        return errorCode
    }
}

object UnknownError : ServiceError{
    override fun message(): String {
        return "An unknown error occurred"
    }

    override fun errorCode(): ErrorCode {
        return ErrorCode.UNKNOWN
    }
}
