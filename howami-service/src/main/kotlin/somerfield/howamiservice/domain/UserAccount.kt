package somerfield.howamiservice.domain

data class UserAccount(val username: String, val passwordHash: String, val emailAddress: String, val state: AccountState)

enum class AccountState {
    PENDING,
    CONFIRMED
}

typealias PasswordHashAlgorithm = (String) -> String