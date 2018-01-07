package somerfield.howami.commsservice.jobs

import somerfield.howami.commsservice.domain.NotificationQueueService
import somerfield.howami.commsservice.domain.UserNotificationService

class SendNotificationsJob(
        private val notificationQueueService: NotificationQueueService,
        private val userNotificationService: UserNotificationService,
        private val notificationConfiguration: NotificationConfiguration = NotificationConfiguration.default()
) {
    fun runJob() {
        notificationQueueService.getPendingNotifications().forEach {
            if (notificationConfiguration.testMode) {
                println("Warning: test mode enabled. Messages will not be sent")
            } else {
                userNotificationService.sendConfirmationRequest(it)
            }
            notificationQueueService.confirmingNotificationSent(it.userId)
        }
    }
}

data class NotificationConfiguration(
    val testMode: Boolean = false
) {
    companion object {
        fun default() = NotificationConfiguration()
    }
}