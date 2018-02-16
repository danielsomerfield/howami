package somerfield.howamiservice.repositories

import com.mongodb.BasicDBObject
import com.mongodb.MongoSocketException
import com.mongodb.MongoWriteException
import com.mongodb.client.MongoCollection
import net.jodah.failsafe.Failsafe
import net.jodah.failsafe.RetryPolicy
import org.bson.Document
import org.bson.types.ObjectId
import somerfield.howamiservice.domain.ErrorResult
import somerfield.howamiservice.domain.Result
import somerfield.howamiservice.domain.accounts.ConfirmationStatus
import somerfield.howamiservice.domain.accounts.RegistrationConfirmation
import somerfield.mongo.appendIfNotNull
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

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

    fun create(registrationConfirmation: RegistrationConfirmation): Result<RegistrationConfirmation, RepositoryError> {
        val dbResult = Result.doTry {
            Failsafe.with<Unit>(RetryPolicy()
                    .retryOn(MongoSocketException::class.java)
                    .withBackoff(100, 2000, TimeUnit.MILLISECONDS)
                    .withMaxRetries(5)
            ).run({ctx ->
                try {
                    registrationConfirmationCollection.insertOne(
                            Document()
                                    .append(userIdField, ObjectId(registrationConfirmation.userId))
                                    .append(confirmationCodeField, registrationConfirmation.confirmationCode)
                                    .append(confirmationStatusField, registrationConfirmation.confirmationStatus.name)
                                    .append(createdDateTimeField, registrationConfirmation.createdDateTime.toEpochMilli())
                    )
                } catch (e: MongoWriteException) {
                    if (recordDoesNotMatch(registrationConfirmation)) {
                        throw e
                    }
                }
            })
        }
        return dbResult
                .map { registrationConfirmation }
                .mapFailure { error ->
                    when (error.exception) {
                        is MongoWriteException -> {
                            if (error.exception.code == 11000) {
                                RepositoryError("Duplicate key", Reason.DUPLICATE_ID)
                            } else {
                                throw RepositoryRuntimeException(error.exception.message ?: "No message provided", error.exception.code)
                            }
                        }
                        else -> throw RepositoryRuntimeException(error.exception.message ?: "No message provided")
                    }
                }
    }

    private fun recordDoesNotMatch(registrationConfirmation: RegistrationConfirmation): Boolean {
        return find(registrationConfirmation.userId) != Optional.of(registrationConfirmation)
    }

    fun delete(userId: String): Boolean {
        return registrationConfirmationCollection.deleteOne(BasicDBObject()
                .append("_id", ObjectId(userId))).deletedCount == 1L
    }

}

data class RepositoryError(override val message: String, val reason: Reason) : ErrorResult

enum class Reason {
    DUPLICATE_ID
}

class RepositoryRuntimeException(message: String, code: Int = RepositoryRuntimeException.UNKNOWN_ERROR) : RuntimeException(message) {
    companion object {
        val UNKNOWN_ERROR = -1
    }
}