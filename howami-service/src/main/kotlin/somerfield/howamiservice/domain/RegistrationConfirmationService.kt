package somerfield.howamiservice.domain

import somerfield.howamiservice.repositories.RegistrationConfirmationRepository

class RegistrationConfirmationService(private val registrationConfirmationRepository: RegistrationConfirmationRepository) {
    fun getAllConfirmations(): List<RegistrationConfirmation> {
        return registrationConfirmationRepository.find()
    }

    fun getConfirmations(status: ConfirmationStatus): List<RegistrationConfirmation> {
        return registrationConfirmationRepository.find(
                status = status
        )
    }

    fun queueConfirmation(emailAddress: String, userId: String) {
//        TODO("NYI")
    }
}