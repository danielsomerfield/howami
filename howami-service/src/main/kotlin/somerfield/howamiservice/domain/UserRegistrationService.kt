package somerfield.howamiservice.domain

class UserRegistrationService {

    fun register(userRegistrationCommand: UserRegistrationCommand): Result<UserRegistration, UserRegistrationError> {
        return Result.Success(UserRegistration("NYI"))
    }
}

data class UserRegistration(val userId: String)
data class UserRegistrationError(val errorCode: String, val message: String)
data class UserRegistrationCommand(val username: String, val password: String, val phoneNumber: String)

sealed class Result<out T, out U> {
    data class Success<out T>(val response: T): Result<T, Nothing>()
    data class Failure<out U>(val errorValue: U): Result<Nothing, U>()
}