package somerfield.howamiservice.domain

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import somerfield.howamiservice.domain.accounts.*
import somerfield.howamiservice.repositories.UserAccountRepository
import java.util.*

class LoginServiceTest {

    private val hash: PasswordHashAlgorithm = String::toUpperCase
    private val validateHash: PasswordValidationAlgorithm = { value, hash -> value.toUpperCase() == hash }

    private val invalidUsername = "invalidUsername"
    private val validUsername = "validUsername"
    private val validPassword = "validPassword"
    private val validPasswordHash = hash(validPassword)
    private val invalidPassword = "invalidPassword"
    private val pendingPassword = "pendingPassword"
    private val pendingPasswordHash = hash(pendingPassword)

    private val pendingUsername = "pending"

    private val userAccountRepository = mock<UserAccountRepository> {
        on {
            findByUsername(validUsername)
        } doReturn (Optional.of(UserAccount(
                username = validUsername,
                emailAddress = "valid@example.com".toEmailAddress().getUnsafe(),
                state = AccountState.CONFIRMED,
                passwordHash = validPasswordHash
        )))

        on {
            findByUsername(pendingUsername)
        } doReturn (Optional.of(UserAccount(
                username = pendingUsername,
                emailAddress = "pending@example.com".toEmailAddress().getUnsafe(),
                state = AccountState.PENDING,
                passwordHash = pendingPasswordHash
        )))
    }

    private val loginService = LoginService(userAccountRepository, validateHash)

    @Test
    fun loginFailureBadUsername() {
        val response = loginService.login(invalidUsername, validPassword)
        assertThat(response, `is`(LoginResponse.FAILED))
    }

    @Test
    fun loginFailureBadPassword() {
        val response = loginService.login(validUsername, invalidPassword)
        assertThat(response, `is`(LoginResponse.FAILED))
    }

    @Test
    fun loginFailureUserNotConfirmed() {
        val response = loginService.login(pendingUsername, pendingPassword)
        assertThat(response, `is`(LoginResponse.FAILED))
    }


    @Test
    fun loginSuccess() {
        val response = loginService.login(validUsername, validPassword)
        assertThat(response, `is`(LoginResponse.SUCCEEDED))
    }

}