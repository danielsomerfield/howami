package somerfield.howamiservice.repositories

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import org.bson.Document
import org.bson.types.ObjectId
import somerfield.howamiservice.domain.accounts.ConfirmationStatus
import somerfield.howamiservice.domain.accounts.RegistrationConfirmation
import somerfield.mongo.appendIfNotNull
import java.time.Instant
import java.util.*

class RegistrationConfirmationRepository(private val registrationConfirmationCollection: MongoCollection<Document>) {

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
                        .append(userIdField, ObjectId(registrationConfirmation.userId))
                        .append(confirmationCodeField, registrationConfirmation.confirmationCode)
                        .append(confirmationStatusField, registrationConfirmation.confirmationStatus.name)
                        .append(createdDateTimeField, registrationConfirmation.createdDateTime.toEpochMilli())
        )
    }

    fun delete(userId: String): Boolean {
        return registrationConfirmationCollection.deleteOne(BasicDBObject()
                .append("_id", ObjectId(userId))).deletedCount == 1L
    }


}