package somerfield.howamiservice.domain

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import somerfield.howamiservice.repositories.RegistrationConfirmationRepository
import java.time.Instant
import java.util.*

class RegistrationConfirmationServiceTest {
    private val repository: RegistrationConfirmationRepository = mock<RegistrationConfirmationRepository> {}

    private val userId = UUID.randomUUID().toString()

    @Test
    fun queueConfirmationHappyPath() {
        val confirmationCode = UUID.randomUUID().toString()
        val now = Instant.now()


        val confirmationService = RegistrationConfirmationService(
                registrationConfirmationRepository = repository,
                confirmationCodeGenerator = { confirmationCode },
                dateTimeSource = { now }
        )

        val emailAddress = "test@example.com"
        confirmationService.queueConfirmation(emailAddress, userId)
        verify(repository).create(RegistrationConfirmation(
                email = emailAddress,
                userId = userId,
                confirmationCode = confirmationCode,
                createdDateTime = now,
                confirmationStatus = ConfirmationStatus.QUEUED
        ))
    }
}