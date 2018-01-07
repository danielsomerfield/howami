package somerfield.howami.commsservice.jobs

import somerfield.howami.commsservice.domain.NotificationQueueService
import somerfield.howami.commsservice.domain.UserNotificationService

class SendNotificationsJob(
        private val notificationQueueService: NotificationQueueService,
        private val userNotificationService: UserNotificationService,
        private val notificationConfiguration: NotificationConfiguration = NotificationConfiguration.default()
) {
    fun runJob() {
        try {
            val pendingNotifications = notificationQueueService.getPendingNotifications()
            pendingNotifications.forEach {
                if (notificationConfiguration.disableSending) {
                    println("Warning: SendNotificationsJob sending disabled, messages will not be sent")
                } else {
                    userNotificationService.sendConfirmationRequest(it)
                }
                notificationQueueService.confirmingNotificationSent(it.userId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class NotificationConfiguration(
    val disableSending: Boolean = false
) {
    companion object {
        fun default() = NotificationConfiguration()
    }
}