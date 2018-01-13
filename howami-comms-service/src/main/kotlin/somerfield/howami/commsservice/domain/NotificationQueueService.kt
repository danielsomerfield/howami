package somerfield.howami.commsservice.domain

import somerfield.howami.commsservice.domain.NotificationStatus.FAILED
import somerfield.howami.commsservice.domain.NotificationStatus.SUCCESS

typealias MessageBuilder = (userId: String, emailAddress: String, confirmationCode: String) -> String

class NotificationQueueService(
        private val userNotificationService: UserNotificationService,
        private val userNotificationEventNotifier: NotificationEventNotifier,
        private val messageBuilder: MessageBuilder,
        private val testMode: () -> Boolean
) {
    fun userRegistered(userRegistrationEvent: UserRegistrationEvent) {
        val message = messageBuilder(
                userRegistrationEvent.userId,
                userRegistrationEvent.emailAddress,
                userRegistrationEvent.confirmationCode
        )

        if (!testMode()) {
            val notifyResult = userNotificationService.sendNotification(userRegistrationEvent.emailAddress, message)
            when (notifyResult.result) {
                SUCCESS -> userNotificationEventNotifier.send(NotificationSentEvent(
                        userId = userRegistrationEvent.userId
                ))
                FAILED -> userNotificationEventNotifier.send(NotificationSendFailedEvent(
                        userId = userRegistrationEvent.userId, errorMessage = notifyResult.message
                ))
            }

        }
    }
}

data class UserRegistrationEvent(
        val userId: String,
        val emailAddress: String,
        val confirmationCode: String
)