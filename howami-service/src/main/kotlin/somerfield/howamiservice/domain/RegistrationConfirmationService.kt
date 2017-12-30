package somerfield.howamiservice.domain

import somerfield.howamiservice.repositories.RegistrationConfirmationRepository
import java.time.Instant

class RegistrationConfirmationService(
        private val registrationConfirmationRepository: RegistrationConfirmationRepository,
        private val confirmationCodeGenerator: () -> String,
        private val dateTimeSource: () -> Instant = Instant::now

) {
    fun getAllConfirmations(): List<RegistrationConfirmation> {
        return registrationConfirmationRepository.find()
    }

    fun queueConfirmation(emailAddress: String, userId: String) {
        registrationConfirmationRepository.create(RegistrationConfirmation(
                email = emailAddress,
                userId = userId,
                confirmationCode = confirmationCodeGenerator(),
                createdDateTime = dateTimeSource(),
                confirmationStatus = ConfirmationStatus.QUEUED
        ))
    }
}