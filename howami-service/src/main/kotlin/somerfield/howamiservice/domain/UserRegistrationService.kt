package somerfield.howamiservice.domain

import java.util.*
import somerfield.howamiservice.repositories.UserAccountRepository

class UserRegistrationService(
        private val userAccountRepository: UserAccountRepository,
        private val idGenerator: () -> String = { UUID.randomUUID().toString() },
        private val hashPassword: (String) -> String = { it }
) {

    fun register(userRegistrationCommand: UserRegistrationCommand): Result<UserRegistration, UserRegistrationError> {

        //TODO: validation

        userAccountRepository.create(UserAccount(
                userRegistrationCommand.username,
                hashPassword(userRegistrationCommand.password),
                userRegistrationCommand.phoneNumber,
                AccountState.PENDING
        ))

        return Result.Success(UserRegistration(
                userId = idGenerator()
        ))
    }
}

data class UserRegistration(val userId: String)
data class UserRegistrationError(val errorCode: String, val message: String)
data class UserRegistrationCommand(val username: String, val password: String, val phoneNumber: String)

sealed class Result<out T, out U> {
    data class Success<out T>(val response: T) : Result<T, Nothing>()
    data class Failure<out U>(val errorValue: U) : Result<Nothing, U>()
}