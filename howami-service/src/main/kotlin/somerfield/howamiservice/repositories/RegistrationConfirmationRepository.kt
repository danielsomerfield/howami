package somerfield.howamiservice.repositories

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import org.bson.Document
import somerfield.howamiservice.domain.ConfirmationStatus
import somerfield.howamiservice.domain.RegistrationConfirmation
import java.time.Instant
import java.util.*

class RegistrationConfirmationRepository(private val registrationConfirmationCollection: MongoCollection<Document>) {

    private val emailField = "email"
    private val userIdField = "_id"
    private val createdDateTimeField = "created-datetime"
    private val confirmationStatusField = "confirmation-status"
    private val confirmationCodeField = "confirmation-code"

    fun find(
            userId: String
    ): Optional<RegistrationConfirmation> {
        return Optional.ofNullable(find(
                userId = userId,
                status = null
        ).firstOrNull())
    }

    fun find(
            userId: String? = null,
            status: ConfirmationStatus? = null
    ): List<RegistrationConfirmation> {
        return registrationConfirmationCollection.find(
                BasicDBObject()
                        .append("_id", userId)
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

    fun delete(userId: String): Boolean {
        return registrationConfirmationCollection.deleteOne(BasicDBObject()
                .append("_id", userId)).deletedCount == 1L
    }
}