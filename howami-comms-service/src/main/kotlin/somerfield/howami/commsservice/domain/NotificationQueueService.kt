package somerfield.howami.commsservice.domain

import somerfield.howami.commsservice.domain.NotificationStatus.FAILED
import somerfield.howami.commsservice.domain.NotificationStatus.SUCCESS

class NotificationQueueService(
        private val userNotificationService: UserNotificationService,
        private val userNotificationEventProducer: NotificationEventProducer,
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
                SUCCESS -> userNotificationEventProducer.send(NotificationSentEvent(
                        userId = userRegistrationEvent.userId
                ))
                FAILED -> userNotificationEventProducer.send(NotificationSendFailedEvent(
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