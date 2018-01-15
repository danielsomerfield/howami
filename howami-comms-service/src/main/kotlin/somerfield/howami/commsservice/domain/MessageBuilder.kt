package somerfield.howami.commsservice.domain

typealias MessageBuilder = (userId: String, emailAddress: String, confirmationCode: String) -> String

object DefaultMessageBuilder {

    val buildMessage: MessageBuilder = { userId, emailAddress, confirmationCode ->
        """
            Welcome to HowAmiI.

            Please confirm your registration.
        """

    }
}