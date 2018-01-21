package somerfield.howamiservice.domain.accounts

import somerfield.howamiservice.domain.ErrorCode
import somerfield.howamiservice.domain.Result
import somerfield.howamiservice.domain.ServiceError
import somerfield.howamiservice.domain.UnknownError
import somerfield.howamiservice.repositories.*

class UserRegistrationService(
        private val userAccountRepository: UserAccountRepository,
        private val registrationConfirmationService: RegistrationConfirmationService,
        private val hashPassword: PasswordHashAlgorithm = { it },
        private val userEventProducer: UserEventProducer
) {

    fun register(userRegistrationCommand: UserRegistrationCommand): Result<UserRegistration, ServiceError> {

        val createResult = userAccountRepository.create(UserAccount(
                userRegistrationCommand.username,
                hashPassword(userRegistrationCommand.password),
                userRegistrationCommand.email,
                AccountState.PENDING
        ))

        return when (createResult) {
            is CreateSuccess -> {
                val userId = createResult.id
                val confirmation = registrationConfirmationService.queueConfirmation(userId)
                userEventProducer.userRegistered(UserRegistrationEvent(
                        userId = userId,
                        emailAddress = userRegistrationCommand.email,
                        confirmationCode = confirmation.confirmationCode
                ))
                Result.Success(UserRegistration(
                        userId = userId
                ))
            }
            is UnexpectedError -> Result.Failure(UnknownError)
            is DuplicateKeyError -> mapDBException(createResult, userRegistrationCommand)
        }
    }

    private fun mapDBException(e: DuplicateKeyError, userRegistrationCommand: UserRegistrationCommand): Result.Failure<ServiceError> {
        return when {
            e.duplicateField == UserAccount::username.name -> Result.Failure(UsernameUnavailableError(username = userRegistrationCommand.username))
            e.duplicateField == UserAccount::emailAddress.name -> Result.Failure(EmailAlreadyRegisteredError(emailAddress = userRegistrationCommand.email))
            else -> Result.Failure(UnknownError)
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
