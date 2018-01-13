package somerfield.howamiservice.domain

import java.time.Instant

data class RegistrationConfirmation(
        val userId: String,
        val confirmationCode: String,
        val createdDateTime: Instant,
        val confirmationStatus: ConfirmationStatus
)