package somerfield.howamiservice.domain

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test
import somerfield.howamiservice.repositories.UserAccountRepository

class UserRegistrationServiceTest {

    @Test
    fun userRegistrationCreatesRecord() {

        val userRegistrationRepository = mock<UserAccountRepository> {
        }

        val expectedId = "expected-id"
        val service = UserRegistrationService(
                userRegistrationRepository,
                idGenerator = { expectedId },
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
                phoneNumber = "1-555-1212",
                state = AccountState.PENDING
        ))
    }
}