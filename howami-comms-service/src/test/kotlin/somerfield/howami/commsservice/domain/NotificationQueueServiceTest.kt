package somerfield.howami.commsservice.domain

import com.nhaarman.mockito_kotlin.*
import org.junit.Test

import java.util.*

class NotificationQueueServiceTest {

    private val userId = UUID.randomUUID().toString()
    private val emailAddress = "${UUID.randomUUID()}@example.com"
    private val badEmail = "bad@example.com"
    private val confirmationCode = UUID.randomUUID().toString()
    private val messageBuilder = { userId: String, emailAddress: String, confirmationCode: String -> "$userId : $confirmationCode" }
    private val successMessage = messageBuilder(userId, emailAddress, confirmationCode)
    private var testMode = false

    private val userNotificationService = mock<UserNotificationService> {
        on {
            sendNotification(any(), any())
        } doReturn (NotificationResponse("failed", NotificationStatus.FAILED))

        on {
            sendNotification(emailAddress, successMessage)
        } doReturn (NotificationResponse("success", NotificationStatus.SUCCESS))
    }
    private val userNotificationEventNotifier = mock<NotificationEventProducer> {}

    private val notificationQueueService = NotificationQueueService(
            userNotificationService = userNotificationService,
            userNotificationEventProducer = userNotificationEventNotifier,
            messageBuilder = messageBuilder,
            testMode = { testMode }
    )

    @Test
    fun userNotificationOnRegistrationEvent() {
        notificationQueueService.userRegistered(UserRegistrationEvent(
                userId = userId,
                emailAddress = emailAddress,
                confirmationCode = confirmationCode
        ));

        verify(userNotificationService).sendNotification(emailAddress, successMessage)
        verify(userNotificationEventNotifier).send(NotificationSentEvent(userId))
    }

    @Test
    fun sendFailure() {
        notificationQueueService.userRegistered(UserRegistrationEvent(
                userId = userId,
                emailAddress = badEmail,
                confirmationCode = confirmationCode
        ));

        verify(userNotificationEventNotifier).send(NotificationSendFailedEvent(userId, "failed"))
    }

    @Test
    fun doesNotSendInTestMode() {
        testMode = true
        notificationQueueService.userRegistered(UserRegistrationEvent(
                userId = userId,
                emailAddress = emailAddress,
                confirmationCode = confirmationCode
        ));

        verifyNoMoreInteractions(userNotificationService)
        verify(userNotificationEventNotifier).send(NotificationSentEvent(userId))
    }
}
