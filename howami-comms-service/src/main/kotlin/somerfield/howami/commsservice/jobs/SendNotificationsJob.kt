package somerfield.howami.commsservice.jobs

import somerfield.howami.commsservice.domain.NotificationQueueService
import somerfield.howami.commsservice.domain.UserNotificationService

class SendNotificationsJob(
        private val notificationQueueService: NotificationQueueService,
        private val userNotificationService: UserNotificationService
) {
    fun runJob() {
        notificationQueueService.getPendingNotifications().forEach {
            userNotificationService.sendConfirmationRequest(it)
            notificationQueueService.confirmingNotificationSent(it.userId)
        }
    }
}