package somerfield.howamiservice.repositories

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import org.bson.Document
import somerfield.howamiservice.domain.ConfirmationStatus
import somerfield.howamiservice.domain.RegistrationConfirmation
import java.time.Instant

class RegistrationConfirmationRepository(private val registrationConfirmationCollection: MongoCollection<Document>) {
    fun find(
            status: ConfirmationStatus? = null
    ): List<RegistrationConfirmation> {
        return registrationConfirmationCollection.find(
                BasicDBObject()
                        .append("status", status)
        ).toList().map {
            RegistrationConfirmation(
                    email = it.getString("email"),
                    userId = it.getString("user-id"),
                    confirmationCode = it.getString("confirmation-code"),
                    createdDateTime = Instant.ofEpochMilli(it.getLong("created-datetime")),
                    confirmationStatus = ConfirmationStatus.valueOf(it.getString("confirmation-status"))
            )
        }
    }
}