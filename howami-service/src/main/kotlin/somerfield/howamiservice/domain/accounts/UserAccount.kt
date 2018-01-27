package somerfield.howamiservice.domain.accounts

data class UserAccount(val username: String, val passwordHash: String, val emailAddress: EmailAddress, val state: AccountState)

enum class AccountState {
    PENDING,
    CONFIRMED
}

typealias PasswordHashAlgorithm = (String) -> String
typealias PasswordValidationAlgorithm = (String, String) -> Boolean