package somerfield.howamiservice.domain.accounts

data class UserRegistrationEvent(
        val userId: String,
        val emailAddress: String,
        val confirmationCode: String
)