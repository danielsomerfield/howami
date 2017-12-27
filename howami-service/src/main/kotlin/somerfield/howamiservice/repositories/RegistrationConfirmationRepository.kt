package somerfield.howamiservice.repositories

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import org.bson.Document
import somerfield.howamiservice.domain.ConfirmationStatus
import somerfield.howamiservice.domain.RegistrationConfirmation
import java.time.Instant

class RegistrationConfirmationRepository(private val registrationConfirmationCollection: MongoCollection<Document>) {

    private val emailField = "email"
    private val userIdField = "user-id"
    private val createdDateTimeField = "created-datetime"
    private val confirmationStatusField = "confirmation-status"
    private val confirmationCodeField = "confirmation-code"

    fun find(
            status: ConfirmationStatus? = null
    ): List<RegistrationConfirmation> {
        return registrationConfirmationCollection.find(
                BasicDBObject()
                        .append("status", status)
        ).toList().map {
            RegistrationConfirmation(
                    email = it.getString(emailField),
                    userId = it.getString(userIdField),
                    confirmationCode = it.getString(this.confirmationCodeField),
                    createdDateTime = Instant.ofEpochMilli(it.getLong(createdDateTimeField)),
                    confirmationStatus = ConfirmationStatus.valueOf(it.getString(confirmationStatusField))
            )
        }
    }

    fun create(registrationConfirmation: RegistrationConfirmation) {
        registrationConfirmationCollection.insertOne(
                Document()
                        .append(emailField, registrationConfirmation.email)
                        .append(userIdField, registrationConfirmation.userId)
                        .append(confirmationCodeField, registrationConfirmation.confirmationCode)
                        .append(confirmationStatusField, registrationConfirmation.confirmationStatus.name)
                        .append(createdDateTimeField, registrationConfirmation.createdDateTime.toEpochMilli())
        )
    }
}