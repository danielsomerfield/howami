package somerfield.howamiservice.domain

import somerfield.howamiservice.repositories.UserAccountRepository

class UserRegistrationService(
        private val userAccountRepository: UserAccountRepository,
        private val registrationConfirmationService: RegistrationConfirmationService,
        private val hashPassword: PasswordHashAlgorithm = { it },
        private val userEventProducer: UserEventProducer
) {

    fun register(userRegistrationCommand: UserRegistrationCommand): Result<UserRegistration, ServiceError> {

        //TODO: validation

        val userId = userAccountRepository.create(UserAccount(
                userRegistrationCommand.username,
                hashPassword(userRegistrationCommand.password),
                userRegistrationCommand.email,
                AccountState.PENDING
        ))

        val confirmation = registrationConfirmationService.queueConfirmation(userId)

        userEventProducer.userRegistered(UserRegistrationEvent(
                userId = userId,
                emailAddress = userRegistrationCommand.email,
                confirmationCode = confirmation.confirmationCode
        ))

        return Result.Success(UserRegistration(
                userId = userId
        ))
    }
}

data class UserRegistration(val userId: String)
data class UserRegistrationCommand(val username: String, val password: String, val email: String)

