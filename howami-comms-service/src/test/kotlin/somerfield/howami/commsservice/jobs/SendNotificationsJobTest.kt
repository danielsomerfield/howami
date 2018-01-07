package somerfield.howami.commsservice.jobs

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import somerfield.howami.commsservice.domain.NotificationQueueService
import somerfield.howami.commsservice.domain.PendingNotification
import somerfield.howami.commsservice.domain.UserNotificationService
import java.util.*

class SendNotificationsJobTest {

    private val userId1 = UUID.randomUUID().toString()
    private val confirmationCode1 = UUID.randomUUID().toString()
    private val emailAddress1 = "user1@example.com"

    private val userId2 = UUID.randomUUID().toString()
    private val confirmationCode2 = UUID.randomUUID().toString()
    private val emailAddress2 = "user2@example.com"

    private val notification1 = PendingNotification(
            userId = userId1,
            email = emailAddress1,
            confirmationCode = confirmationCode1
    )

    private val notification2 = PendingNotification(
            userId = userId2,
            email = emailAddress2,
            confirmationCode = confirmationCode2
    )

    private val notificationQueueService = mock<NotificationQueueService> {
        on {
            getPendingNotifications()
        } doReturn (listOf(
                notification1,
                notification2
        ))
    }

    private val userNotificationService = mock<UserNotificationService> {

    }

    @Test
    fun testUnsentNotificationsSent() {
        SendNotificationsJob(
                notificationQueueService = notificationQueueService,
                userNotificationService = userNotificationService
        ).runJob()

        verify(notificationQueueService).confirmingNotificationSent(userId1)
        verify(notificationQueueService).confirmingNotificationSent(userId2)
        verify(userNotificationService).sendConfirmationRequest(notification1)
        verify(userNotificationService).sendConfirmationRequest(notification2)
    }

    //TODO: rety if failed
    //TODO: test (non send) mode
    //TODO: what do we do if there is a failure to update the sent status? Local cache of status?
}