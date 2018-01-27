package somerfield.howamiservice.domain

@Suppress("UNUSED")
sealed class Result<out T, out E : ErrorResult> {

    data class Success<out T>(val response: T) : Result<T, Nothing>()

    data class Failure<out E : ErrorResult>(val errorValue: E) : Result<Nothing, E>()

    companion object {
        fun <T> doTry(fn: () -> T): Result<T, ExceptionErrorResult> {
            return try {
                Success(fn())
            } catch (e: Exception) {
                Failure(ExceptionErrorResult(e))
            }
        }
    }

    fun <U> map(fn: (T) -> U): Result<U, E> {
        return flatMap { value -> Success(fn(value)) }
    }

    fun <U, E : ErrorResult> flatMap(fn: (T) -> Result<U, E>): Result<U, E> {
        @Suppress("UNCHECKED_CAST")
        return when (this) {
            is Success -> fn(this.response)
            is Failure -> Failure(this.errorValue) as Failure<E>
        }
    }

    fun getOrThrow(exception: Exception): T {
        return when (this) {
            is Success -> this.response
            is Failure -> throw exception
        }
    }

    fun getUnsafe() = getOrThrow(RuntimeException("Expected value but got failure"))
}

data class ExceptionErrorResult(
        private val exception: Exception
) : ErrorResult {
    override val message: String
        get() = exception.message ?: "Unknown error"

    override fun errorCode() = ErrorCode.UNKNOWN
}

//TODO: remove error code
interface ErrorResult {
    val message: String
    fun errorCode(): ErrorCode
}

data class BasicErrorResult(private val errorCode: ErrorCode, override val message: String) : ErrorResult {

    override fun errorCode(): ErrorCode {
        return errorCode
    }
}

object UnknownErrorResult : ErrorResult {

    override val message: String
        get() = "Unknown error"

    override fun errorCode(): ErrorCode {
        return ErrorCode.UNKNOWN
    }
}
