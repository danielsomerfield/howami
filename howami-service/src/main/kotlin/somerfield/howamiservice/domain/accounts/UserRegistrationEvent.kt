package somerfield.howamiservice.domain.accounts

data class UserRegistrationEvent(
        val userId: String,
        val emailAddress: EmailAddress,
        val confirmationCode: String
)