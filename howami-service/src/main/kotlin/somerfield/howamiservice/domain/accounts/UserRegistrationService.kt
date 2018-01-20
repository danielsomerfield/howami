package somerfield.howamiservice.domain.accounts

import com.mongodb.DuplicateKeyException
import somerfield.howamiservice.domain.ErrorCode
import somerfield.howamiservice.domain.Result
import somerfield.howamiservice.domain.ServiceError
import somerfield.howamiservice.domain.UnknownError
import somerfield.howamiservice.repositories.UserAccountRepository

class UserRegistrationService(
        private val userAccountRepository: UserAccountRepository,
        private val registrationConfirmationService: RegistrationConfirmationService,
        private val hashPassword: PasswordHashAlgorithm = { it },
        private val userEventProducer: UserEventProducer
) {

    fun register(userRegistrationCommand: UserRegistrationCommand): Result<UserRegistration, ServiceError> {

        try {
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
        } catch (e: DuplicateKeyException) {
            return mapDBException(userRegistrationCommand)
        }
    }

    private fun mapDBException(userRegistrationCommand: UserRegistrationCommand): Result.Failure<ServiceError> {
        return if (userAccountRepository.findByUsername(username = userRegistrationCommand.username).isPresent) {
            Result.Failure(UsernameUnavailableError(username = userRegistrationCommand.username))
        } else if (userAccountRepository.findByEmailAddress(emailAddress = userRegistrationCommand.email).isPresent) {
            Result.Failure(EmailAlreadyRegisteredError(emailAddress = userRegistrationCommand.email))
        } else {
            Result.Failure(UnknownError)
        }
    }
}

data class UserRegistration(val userId: String)
data class UserRegistrationCommand(val username: String, val password: String, val email: String)

data class UsernameUnavailableError(val username: String) : ServiceError {
    override fun message() = "The username $username is already taken."
    override fun errorCode() = ErrorCode.USERNAME_UNAVAILABLE
}

data class EmailAlreadyRegisteredError(val emailAddress: String) : ServiceError {
    override fun message() = "An account is already registered under email $emailAddress."
    override fun errorCode() = ErrorCode.EMAIL_ALREADY_REGISTERED
}
