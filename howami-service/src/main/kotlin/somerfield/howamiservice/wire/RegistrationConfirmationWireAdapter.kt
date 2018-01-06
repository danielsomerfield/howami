package somerfield.howamiservice.wire

import somerfield.howamiservice.domain.RegistrationConfirmation
import java.time.format.DateTimeFormatter

object RegistrationConfirmationWireAdapter {
    fun toWireType(confirmations: List<RegistrationConfirmation>): List<RegistrationConfirmationWireType> {
        return confirmations.map {
            toWireType(it)
        }
    }

    private fun toWireType(it: RegistrationConfirmation): RegistrationConfirmationWireType {
        return RegistrationConfirmationWireType(
                it.email,
                it.userId,
                it.confirmationCode,
                DateTimeFormatter.ISO_INSTANT.format(it.createdDateTime),
                it.confirmationStatus.name
        )
    }
}