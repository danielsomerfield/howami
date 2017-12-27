package somerfield.howamiservice.domain

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Test
import somerfield.howamiservice.repositories.UserAccountRepository
import java.util.*

class UserRegistrationServiceTest {

    val generatedUserId = UUID.randomUUID().toString()
    private val userRegistrationRepository = mock<UserAccountRepository> {
        on {
            create(any())
        } doReturn (generatedUserId)
    }

    private val registrationConfirmationService = mock<RegistrationConfirmationService> {

    }

    private val userRegistrationService = UserRegistrationService(
            userAccountRepository = userRegistrationRepository,
            registrationConfirmationService = registrationConfirmationService,
            hashPassword = { pwd -> pwd.toUpperCase() }
    )

    @Test
    fun userRegistrationCreatesRecord() {

        val result = userRegistrationService.register(UserRegistrationCommand("uname", "pwd", "foo@example.com"))
        when (result) {
            is Result.Success -> assertThat(result.response.userId, `is`(generatedUserId))
            else -> fail()
        }
        verify(userRegistrationRepository).create(UserAccount(
                username = "uname",
                passwordHash = "PWD",
                emailAddress = "foo@example.com",
                state = AccountState.PENDING
        ))
    }

    @Test
    fun userRegistrationSendsConfirmationRequest() {
        userRegistrationService.register(UserRegistrationCommand("uname", "pwd", "foo@example.com"))
        verify(registrationConfirmationService).queueConfirmation(
                emailAddress = "foo@example.com",
                userId = generatedUserId
        )
    }

    //TODO: validations
    @Test
    @Ignore
    fun userRegistrationFailsForDuplicateEmail() {

    }

    @Test
    @Ignore
    fun userRegistrationFailsForInvalidPhone() {

    }

    @Test
    @Ignore
    fun userRegistrationFailsForInvalidEmail() {

    }
}