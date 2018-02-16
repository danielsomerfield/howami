package somerfield.howamiservice.repositories

import com.github.fakemongo.Fongo
import com.mongodb.MongoSocketException
import com.mongodb.ServerAddress
import com.mongodb.client.MongoCollection
import org.bson.Document
import org.bson.types.ObjectId
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import somerfield.howamiservice.domain.Result
import somerfield.howamiservice.domain.accounts.ConfirmationStatus
import somerfield.howamiservice.domain.accounts.RegistrationConfirmation
import somerfield.testing.IntegrationTests
import java.time.Instant.now
import java.util.*

@Category(IntegrationTests::class)
class RegistrationConfirmationRepositoryDurabilityTest {

    private val userId3 = ObjectId.get()
    private val createdDateTime3 = now()
    private val confirmationStatus3 = ConfirmationStatus.CONFIRMED
    private val confirmationCode3 = UUID.randomUUID().toString()

    private val databaseName = "howami"

    private val mongo = Fongo("mock-mongo")

    private var registrationConfirmationCollection: MongoCollection<Document>? = null

    @Before
    fun setup() {
        mongo.dropDatabase(databaseName)
        registrationConfirmationCollection = mongo.getDatabase(databaseName).getCollection("registration_confirmation")
    }

    @Test
    fun networkFailuresAreRetriedOnCreate() {
        registrationConfirmationCollection = doStub(
                actual = registrationConfirmationCollection!!,
                insertOneStub = { _, _ -> throw MongoSocketException("", ServerAddress()) }
        )
        val repository = RegistrationConfirmationRepository(registrationConfirmationCollection!!)
        repository.create(RegistrationConfirmation(userId3.toString(), confirmationCode3, createdDateTime3, confirmationStatus3))
        assertThat(repository.find(
                userId = userId3.toString()
        ), `is`(Optional.of(RegistrationConfirmation(userId3.toString(), confirmationCode3, createdDateTime3, confirmationStatus3))))
    }

    @Test
    fun retryWithSuccessfulWriteSucceeds() {
        registrationConfirmationCollection = doStub(
                actual = registrationConfirmationCollection!!,
                insertOneStub = { actual, document ->
                    actual.insertOne(document);
                    throw MongoSocketException("", ServerAddress())
                }
        )

        val repository = RegistrationConfirmationRepository(registrationConfirmationCollection!!)
        val result = repository.create(RegistrationConfirmation(userId3.toString(), confirmationCode3, createdDateTime3, confirmationStatus3))
        when (result) {
            is Result.Failure -> Assert.fail()
        }
        assertThat(repository.find(
                userId = userId3.toString()
        ), `is`(Optional.of(RegistrationConfirmation(userId3.toString(), confirmationCode3, createdDateTime3, confirmationStatus3))))
    }
}

fun doStub(actual: MongoCollection<Document>, insertOneStub: (MongoCollection<Document>, document: Document) -> Unit): MongoCollection<Document> {
    return object : MongoCollection<Document> by actual {
        var callCount = 0
        override fun insertOne(document: Document) {
            if (callCount == 0) {
                callCount++
                return insertOneStub(actual, document)
            } else {
                return actual.insertOne(document)
            }
        }
    }
}