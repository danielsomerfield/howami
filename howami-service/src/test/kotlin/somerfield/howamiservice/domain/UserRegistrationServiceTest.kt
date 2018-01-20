package somerfield.howamiservice.domain

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.`when`
import somerfield.howamiservice.domain.accounts.*
import somerfield.howamiservice.repositories.UserAccountRepository
import java.time.Instant
import java.util.*

class UserRegistrationServiceTest {

    //Mocked data
    private val generatedUserId = UUID.randomUUID().toString()
    private val confirmationCode = UUID.randomUUID().toString()
    private val now = Instant.now()

    //Mocks
    private val userRegistrationRepository = mock<UserAccountRepository> {
        on {
            create(any())
        } doReturn (generatedUserId)
    }

    private val registrationConfirmationService = mock<RegistrationConfirmationService> {

    }

    private val userEventProducer = mock<UserEventProducer> {

    }

    private val userRegistrationService = UserRegistrationService(
            userAccountRepository = userRegistrationRepository,
            registrationConfirmationService = registrationConfirmationService,
            hashPassword = { pwd -> pwd.toUpperCase() },
            userEventProducer = userEventProducer
    )

    @Test
    fun userRegistrationHappyPath() {

        val username = "uname"
        val emailAddress = "foo@example.com"

        `when`(registrationConfirmationService.queueConfirmation(
                userId = generatedUserId
        )).thenReturn(RegistrationConfirmation(
                userId = generatedUserId,
                confirmationCode = confirmationCode,
                createdDateTime = now,
                confirmationStatus = ConfirmationStatus.UNCONFIRMED
        ))

        val result = userRegistrationService.register(UserRegistrationCommand(username, "pwd", emailAddress))
        when (result) {
            is Result.Success -> assertThat(result.response.userId, `is`(generatedUserId))
            else -> fail()
        }

        verify(userRegistrationRepository).create(UserAccount(
                username = username,
                passwordHash = "PWD",
                emailAddress = emailAddress,
                state = AccountState.PENDING
        ))

        verify(userEventProducer).userRegistered(UserRegistrationEvent(
                userId = generatedUserId,
                emailAddress = emailAddress,
                confirmationCode = confirmationCode
        ))
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