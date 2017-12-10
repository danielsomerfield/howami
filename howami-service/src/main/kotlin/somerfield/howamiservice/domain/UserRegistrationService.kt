package somerfield.howamiservice.domain

import somerfield.howamiservice.repositories.UserAccountRepository

class UserRegistrationService(
        private val userAccountRepository: UserAccountRepository,
        private val hashPassword: (String) -> String = { it }
) {

    fun register(userRegistrationCommand: UserRegistrationCommand): Result<UserRegistration, UserRegistrationError> {

        //TODO: validation

        val id = userAccountRepository.create(UserAccount(
                userRegistrationCommand.username,
                hashPassword(userRegistrationCommand.password),
                userRegistrationCommand.email,
                AccountState.PENDING
        ))

        return Result.Success(UserRegistration(
                userId = id
        ))
    }
}

data class UserRegistration(val userId: String)
data class UserRegistrationError(val errorCode: String, val message: String)
data class UserRegistrationCommand(val username: String, val password: String, val email: String)

sealed class Result<out T, out U> {
    data class Success<out T>(val response: T) : Result<T, Nothing>()
    data class Failure<out U>(val errorValue: U) : Result<Nothing, U>()
}