package somerfield.howamiservice.domain

import java.util.*

data class RegistrationConfirmation(
        val email: String,
        val userId: String,
        val confirmationCode: String,
        val createdDateTime: Date,
        val confirmationStatus: ConfirmationStatus
)