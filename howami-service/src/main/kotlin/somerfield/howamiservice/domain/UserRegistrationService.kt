package somerfield.howamiservice.domain

import somerfield.howamiservice.repositories.UserAccountRepository

class UserRegistrationService(
        private val userAccountRepository: UserAccountRepository,
        private val hashPassword: (String) -> String = { it }
) {

    fun register(userRegistrationCommand: UserRegistrationCommand): Result<UserRegistration, ServiceError> {

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
data class UserRegistrationCommand(val username: String, val password: String, val email: String)

