package somerfield.howamiservice.domain.accounts

import somerfield.howamiservice.domain.accounts.ConfirmationResult.*
import somerfield.howamiservice.repositories.RegistrationConfirmationRepository
import somerfield.howamiservice.repositories.UserAccountRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class RegistrationConfirmationService(
        private val registrationConfirmationRepository: RegistrationConfirmationRepository,
        private val userAccountRepository: UserAccountRepository,
        private val confirmationCodeGenerator: () -> String,
        private val dateTimeSource: () -> Instant = Instant::now

) {
    fun getAllConfirmations(): List<RegistrationConfirmation> {
        return registrationConfirmationRepository.find()
    }

    fun queueConfirmation(userId: String): RegistrationConfirmation {
        val newConfirmation = RegistrationConfirmation(
                userId = userId,
                confirmationCode = confirmationCodeGenerator(),
                createdDateTime = dateTimeSource(),
                confirmationStatus = ConfirmationStatus.UNCONFIRMED
        )
        registrationConfirmationRepository.create(newConfirmation)
        return newConfirmation;
    }

    fun confirm(userId: String, confirmationCode: String): ConfirmationResult {
        val confirmation = registrationConfirmationRepository.find(userId)
        if (confirmation.isPresent) {
            if (confirmationMatches(confirmation, confirmationCode)) {
                userAccountRepository.update(userId, AccountState.CONFIRMED)
                return if (isExpired(confirmation.get())) {
                    EXPIRED
                } else {
                    registrationConfirmationRepository.delete(userId)
                    CONFIRMED
                }
            }
        }
        return INVALID
    }

    private fun isExpired(confirmation: RegistrationConfirmation) = confirmation.createdDateTime
            .isBefore(Instant.now().minus(2, ChronoUnit.HOURS))

    private fun confirmationMatches(confirmation: Optional<RegistrationConfirmation>, confirmationCode: String) =
            confirmation.get().confirmationCode == confirmationCode

    fun getUnsentConfirmations(): List<RegistrationConfirmation> {
        return registrationConfirmationRepository.find(
                status = ConfirmationStatus.UNCONFIRMED
        )
    }
}

enum class ConfirmationResult {
    CONFIRMED,
    EXPIRED,
    INVALID
}