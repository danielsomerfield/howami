package somerfield.howamiservice.domain

import com.nhaarman.mockito_kotlin.*
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Ignore
import org.junit.Test
import somerfield.howamiservice.domain.accounts.*
import somerfield.howamiservice.domain.accounts.ConfirmationStatus.CONFIRMED
import somerfield.howamiservice.domain.accounts.ConfirmationStatus.UNCONFIRMED
import somerfield.howamiservice.repositories.RegistrationConfirmationRepository
import somerfield.howamiservice.repositories.UserAccountRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
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
                confirmationStatus = UNCONFIRMED
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

    @Test
    fun approvalFailsWithExpiredCode() {
        val confirmationCode = UUID.randomUUID().toString()
        val initialRegistrationTime = Instant.now().minus(1, ChronoUnit.DAYS)
        val result = service(confirmationCode, initialRegistrationTime).confirm(userId, confirmationCode)
        assertThat(result, `is`(ConfirmationResult.EXPIRED))
    }

    @Test
    fun approvalFailsWithNonExistentConfirmation() {
        val confirmationCode = UUID.randomUUID().toString()
        val initialRegistrationTime = Instant.now()
        val result = service(confirmationCode, initialRegistrationTime).confirm("other", confirmationCode)
        assertThat(result, `is`(ConfirmationResult.INVALID))
    }

    private fun service(confirmationCode: String, timeCreated: Instant, confirmationStatus: ConfirmationStatus = UNCONFIRMED): RegistrationConfirmationService {

        whenever(registrationConfirmationRepository.find(any())).thenReturn(Optional.empty())

        whenever(registrationConfirmationRepository.find(userId)).thenReturn(
                Optional.of(RegistrationConfirmation(
                        userId = userId,
                        confirmationCode = confirmationCode,
                        createdDateTime = timeCreated,
                        confirmationStatus = confirmationStatus
                ))
        )

        return RegistrationConfirmationService(
                registrationConfirmationRepository = registrationConfirmationRepository,
                userAccountRepository = userAccountRepository,
                confirmationCodeGenerator = { confirmationCode },
                dateTimeSource = { timeCreated }
        )
    }

}