package somerfield.howamiservice.domain

import com.nhaarman.mockito_kotlin.*
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Ignore
import org.junit.Test
import somerfield.howamiservice.repositories.RegistrationConfirmationRepository
import somerfield.howamiservice.repositories.UserAccountRepository
import java.time.Instant
import java.util.*

class RegistrationConfirmationServiceTest {
    private val registrationConfirmationRepository: RegistrationConfirmationRepository = mock {}
    private val userAccountRepository: UserAccountRepository = mock {}

    private val userId = UUID.randomUUID().toString()

    @Test
    fun queueConfirmationHappyPath() {
        val confirmationCode = UUID.randomUUID().toString()
        val now = Instant.now()

        val confirmationService = service(confirmationCode, now)

        val confirmation = confirmationService.queueConfirmation(userId)
        val expectedConfirmation = RegistrationConfirmation(
                userId = userId,
                confirmationCode = confirmationCode,
                createdDateTime = now,
                confirmationStatus = ConfirmationStatus.UNCONFIRMED
        )
        verify(registrationConfirmationRepository).create(expectedConfirmation)

        assertThat(confirmation, `is`(expectedConfirmation))
    }

    @Test
    fun userConfirmSucceedsWithExistingConfirmation() {
        val confirmationCode = UUID.randomUUID().toString()
        val now = Instant.now()

        val result = service(confirmationCode, now).confirm(userId, confirmationCode)
        assertThat(result, `is`(ConfirmationResult.CONFIRMED))

        verify(userAccountRepository).update(userId, state = AccountState.CONFIRMED)
        verify(registrationConfirmationRepository).delete(userId = userId)
    }

    @Test
    fun approvalFailsWithInvalidCode() {
        val confirmationCode = UUID.randomUUID().toString()
        val now = Instant.now()

        val result = service(confirmationCode, now).confirm(userId, confirmationCode + "WRONG")
        assertThat(result, `is`(ConfirmationResult.INVALID))

        verifyNoMoreInteractions(userAccountRepository)
        verify(registrationConfirmationRepository, never()).delete(any())
    }

    private fun service(confirmationCode: String, now: Instant): RegistrationConfirmationService {
        whenever(registrationConfirmationRepository.find(userId)).thenReturn(
                Optional.of(RegistrationConfirmation(
                        userId = userId,
                        confirmationCode = confirmationCode,
                        createdDateTime = now,
                        confirmationStatus = ConfirmationStatus.UNCONFIRMED
                ))
        )
        return RegistrationConfirmationService(
                registrationConfirmationRepository = registrationConfirmationRepository,
                userAccountRepository = userAccountRepository,
                confirmationCodeGenerator = { confirmationCode },
                dateTimeSource = { now }
        )
    }

    @Test
    @Ignore
    fun approvalFailsWithExpiredCode() {

    }

    @Test
    @Ignore
    fun approvalFailsIfAlreadyConfirmed() {

    }
}