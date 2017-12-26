package somerfield.howamiservice.domain

class RegistrationConfirmationService {
    fun getAllConfirmations(): Result<List<RegistrationConfirmation>, ServiceError> {
        TODO()
    }

    fun getConfirmations(status: ConfirmationStatus): Result<List<RegistrationConfirmation>, ServiceError> {
        TODO("NYI")
    }
}