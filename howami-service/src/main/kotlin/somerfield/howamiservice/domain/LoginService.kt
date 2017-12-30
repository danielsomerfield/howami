package somerfield.howamiservice.domain

import somerfield.howamiservice.repositories.UserAccountRepository

class LoginService(
        private val userAccountRepository: UserAccountRepository,
        private val hash: PasswordHashAlgorithm

) {
    fun login(username: String, password: String): LoginResponse {
        return if (userAccountRepository.find(username)
                .filter { it.state == AccountState.CONFIRMED }
                .filter { passwordMatches(password, it) }.isPresent) LoginResponse.SUCCEEDED else LoginResponse.FAILED
    }

    private fun passwordMatches(password: String, user: UserAccount) =
            hash(password) == user.passwordHash

}

enum class LoginResponse {
    FAILED,
    SUCCEEDED
}