package somerfield.howamiservice.domain

import somerfield.howamiservice.domain.ConfirmationResult.CONFIRMED
import somerfield.howamiservice.domain.ConfirmationResult.INVALID
import somerfield.howamiservice.repositories.RegistrationConfirmationRepository
import somerfield.howamiservice.repositories.UserAccountRepository
import java.time.Instant

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
        return if (confirmation.isPresent && confirmation.get().confirmationCode == confirmationCode) {
            userAccountRepository.update(userId, AccountState.CONFIRMED)
            registrationConfirmationRepository.delete(userId)
            CONFIRMED
        } else {
            INVALID
        }
    }

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