package somerfield.howamiservice.repositories

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import org.bson.Document
import org.bson.types.ObjectId
import somerfield.howamiservice.domain.ConfirmationStatus
import somerfield.howamiservice.domain.RegistrationConfirmation
import java.time.Instant
import java.util.*

class RegistrationConfirmationRepository(private val registrationConfirmationCollection: MongoCollection<Document>) {

    private val emailField = "email"
    private val userIdField = "_id"
    private val createdDateTimeField = "created_datetime"
    private val confirmationStatusField = "confirmation_status"
    private val confirmationCodeField = "confirmation_code"

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
                        .appendIfNotNull(userIdField, userId, { ObjectId(it) })
                        .appendIfNotNull(confirmationStatusField, status, { it.name })
        ).toList().map {
            RegistrationConfirmation(
                    email = it.getString(emailField),
                    userId = it.getObjectId(userIdField).toString(),
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
                        .append(userIdField, ObjectId(registrationConfirmation.userId))
                        .append(confirmationCodeField, registrationConfirmation.confirmationCode)
                        .append(confirmationStatusField, registrationConfirmation.confirmationStatus.name)
                        .append(createdDateTimeField, registrationConfirmation.createdDateTime.toEpochMilli())
        )
    }

    fun delete(userId: String): Boolean {
        return registrationConfirmationCollection.deleteOne(BasicDBObject()
                .append("_id", userId)).deletedCount == 1L
    }

    private fun <T, U> BasicDBObject.appendIfNotNull(field: String, value: T?, converter: (T) -> U): BasicDBObject {
        value?.let { this.append(field, converter(value)) }
        return this
    }
}