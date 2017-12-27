package somerfield.howamiservice.domain

import java.time.Instant
import java.util.*

data class RegistrationConfirmation(
        val email: String,
        val userId: String,
        val confirmationCode: String,
        val createdDateTime: Instant,
        val confirmationStatus: ConfirmationStatus
)