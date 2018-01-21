package somerfield.howamiservice.domain

import com.nhaarman.mockito_kotlin.*
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.`when`
import somerfield.howamiservice.domain.accounts.*
import somerfield.howamiservice.repositories.CreateSuccess
import somerfield.howamiservice.repositories.DuplicateKeyError
import somerfield.howamiservice.repositories.UserAccountRepository
import java.time.Instant
import java.util.*

class UserRegistrationServiceTest {

    //Mocked data
    private val generatedUserId = UUID.randomUUID().toString()
    private val confirmationCode = UUID.randomUUID().toString()
    private val now = Instant.now()

    private val alreadyRegisteredUser = UserAccount(
            username = UUID.randomUUID().toString(),
            passwordHash = UUID.randomUUID().toString(),
            emailAddress = "${UUID.randomUUID()}@example.com",
            state = AccountState.CONFIRMED
    )

    //Mocks
    private val userRegistrationRepository = mock<UserAccountRepository> {
        on {
            create(any())
        } doReturn (CreateSuccess(generatedUserId))

        on {
            findByUsername(username = any())
        } doReturn (Optional.empty())
    }

    private val registrationConfirmationService = mock<RegistrationConfirmationService> {}

    private val userEventProducer = mock<UserEventProducer> {}

    private val userRegistrationService = UserRegistrationService(
            userAccountRepository = userRegistrationRepository,
            registrationConfirmationService = registrationConfirmationService,
            hashPassword = { pwd -> pwd.toUpperCase() },
            userEventProducer = userEventProducer
    )

    //Tests

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

    @Test
    fun userRegistrationFailsForDuplicateUsername() {
        val proposedUsername = UUID.randomUUID().toString()
        whenever(userRegistrationRepository.create(any())).thenReturn(DuplicateKeyError(UserAccount::username.name))
        whenever(userRegistrationRepository.findByUsername(username = any())).thenReturn(Optional.of(alreadyRegisteredUser))

        val result = userRegistrationService.register(
                UserRegistrationCommand(username = proposedUsername, password = "pwd", email = "test@example.com")
        )

        @Suppress("UNCHECKED_CAST")
        assertThat(result as Result.Failure<UsernameUnavailableError>, `is`(Result.Failure(UsernameUnavailableError(proposedUsername))))
    }

    @Test
    fun userRegistrationFailsForDuplicateEmail() {
        val proposedEmailAddress = alreadyRegisteredUser.emailAddress
        whenever(userRegistrationRepository.create(any())).thenReturn(DuplicateKeyError(UserAccount::emailAddress.name))
        whenever(userRegistrationRepository.findByEmailAddress(emailAddress = proposedEmailAddress)).thenReturn(Optional.of(alreadyRegisteredUser))

        val result = userRegistrationService.register(
                UserRegistrationCommand(username = "whatever", password = "pwd", email = proposedEmailAddress)
        )

        @Suppress("UNCHECKED_CAST")
        assertThat(result as Result.Failure<EmailAlreadyRegisteredError>, `is`(Result.Failure(EmailAlreadyRegisteredError(proposedEmailAddress))))
    }

}