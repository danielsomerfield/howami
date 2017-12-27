package somerfield.howamiservice.domain

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test
import somerfield.howamiservice.repositories.UserAccountRepository

class UserRegistrationServiceTest {

    @Test
    fun userRegistrationCreatesRecord() {

        val expectedId = "expected-id"
        val userRegistrationRepository = mock<UserAccountRepository> {
            on {
                create(any())
            } doReturn (expectedId)
        }

        val service = UserRegistrationService(
                userRegistrationRepository,
                hashPassword = { pwd -> pwd.toUpperCase() }
        )
        val cmd = UserRegistrationCommand("uname", "pwd", "1-555-1212")
        val result = service.register(cmd)
        when (result) {
            is Result.Success -> assertThat(result.response.userId, `is`(expectedId))
            else -> fail()
        }

        verify(userRegistrationRepository).create(UserAccount(
                username = "uname",
                passwordHash = "PWD",
                emailAddress = "1-555-1212",
                state = AccountState.PENDING
        ))
    }
}