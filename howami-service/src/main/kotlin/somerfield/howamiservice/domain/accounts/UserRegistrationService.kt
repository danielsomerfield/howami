package somerfield.howamiservice.domain.accounts

import somerfield.howamiservice.domain.*
import somerfield.howamiservice.repositories.CreateSuccess
import somerfield.howamiservice.repositories.DuplicateKeyError
import somerfield.howamiservice.repositories.UnexpectedDBError
import somerfield.howamiservice.repositories.UserAccountRepository

class UserRegistrationService(
        private val userAccountRepository: UserAccountRepository,
        private val registrationConfirmationService: RegistrationConfirmationService,
        private val hashPassword: PasswordHashAlgorithm = { it },
        private val userEventProducer: UserEventProducer
) {

    fun register(userRegistrationCommand: UserRegistrationCommand): Result<UserRegistration, ErrorResult> {

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
            is UnexpectedDBError -> Result.Failure(UnknownErrorResult)
            is DuplicateKeyError -> mapRepositoryException(createResult, userRegistrationCommand)
        }
    }

    private fun mapRepositoryException(e: DuplicateKeyError, userRegistrationCommand: UserRegistrationCommand): Result.Failure<ErrorResult> {
        return when {
            e.duplicateField == UserAccount::username.name -> Result.Failure(UsernameUnavailableErrorResult(username = userRegistrationCommand.username))
            e.duplicateField == UserAccount::emailAddress.name ->
                Result.Failure(EmailAlreadyRegisteredErrorResult(emailAddress = userRegistrationCommand.email))
            else -> Result.Failure(UnknownErrorResult)
        }
    }
}

data class UserRegistration(val userId: String)
data class UserRegistrationCommand(val username: String, val password: String, val email: EmailAddress)

data class UsernameUnavailableErrorResult(val username: String) : ErrorCodeErrorResult {
    override val errorCode: ErrorCode
        get() = ErrorCode.USERNAME_UNAVAILABLE

    override val message: String
        get() = "The username $username is already taken."

}

data class EmailAlreadyRegisteredErrorResult(val emailAddress: EmailAddress) : ErrorCodeErrorResult {

    override val errorCode: ErrorCode
        get() = ErrorCode.EMAIL_ALREADY_REGISTERED

    override val message: String
        get() = "An account is already registered under email $emailAddress."

}
