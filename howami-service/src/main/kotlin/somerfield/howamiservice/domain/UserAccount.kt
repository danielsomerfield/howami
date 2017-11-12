package somerfield.howamiservice.domain

data class UserAccount(val username: String, val passwordHash: String, val phoneNumber: String, val state: AccountState)

enum class AccountState {
    PENDING
}
