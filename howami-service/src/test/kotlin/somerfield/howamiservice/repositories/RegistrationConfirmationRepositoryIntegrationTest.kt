package somerfield.howamiservice.repositories

import com.github.fakemongo.Fongo
import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import org.bson.Document
import org.bson.types.ObjectId
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.experimental.categories.Category
import somerfield.howamiservice.domain.Result
import somerfield.howamiservice.domain.accounts.ConfirmationStatus.CONFIRMED
import somerfield.howamiservice.domain.accounts.ConfirmationStatus.UNCONFIRMED
import somerfield.howamiservice.domain.accounts.RegistrationConfirmation
import somerfield.howamiservice.repositories.Reason.DUPLICATE_ID
import somerfield.testing.IntegrationTests
import java.time.Instant.now
import java.util.*

@Category(IntegrationTests::class)
class RegistrationConfirmationRepositoryIntegrationTest {

    private val userIdField = "_id"
    private val createdDateTimeField = "created_datetime"
    private val confirmationStatusField = "confirmation_status"
    private val confirmationCodeField = "confirmation_code"

    private val databaseName = "howami"

    private val mongo = Fongo("mock-mongo")

    private var repository: RegistrationConfirmationRepository? = null

    private var registrationConfirmationCollection: MongoCollection<Document>? = null

    private val userId1 = ObjectId.get()
    private val createdDateTime1 = now()
    private val confirmationStatus1 = UNCONFIRMED
    private val confirmationCode1 = UUID.randomUUID().toString()

    private val userId2 = ObjectId.get()
    private val createdDateTime2 = now()
    private val confirmationStatus2 = CONFIRMED
    private val confirmationCode2 = UUID.randomUUID().toString()

    private val expected1 = RegistrationConfirmation(
            userId = userId1.toString(),
            confirmationCode = confirmationCode1,
            createdDateTime = createdDateTime1,
            confirmationStatus = confirmationStatus1
    )

    private val expected2 = RegistrationConfirmation(
            userId = userId2.toString(),
            confirmationCode = confirmationCode2,
            createdDateTime = createdDateTime2,
            confirmationStatus = confirmationStatus2
    )

    @Before
    fun setup() {
        mongo.dropDatabase(databaseName)
        registrationConfirmationCollection = mongo.getDatabase(databaseName).getCollection("registration_confirmation")

        repository = RegistrationConfirmationRepository(registrationConfirmationCollection!!)

        val user1Doc = Document(mapOf(
                userIdField to userId1,
                createdDateTimeField to createdDateTime1.toEpochMilli(),
                confirmationStatusField to confirmationStatus1.toString(),
                confirmationCodeField to confirmationCode1
        ))
        val user2Doc = Document(mapOf(
                userIdField to userId2,
                createdDateTimeField to createdDateTime2.toEpochMilli(),
                confirmationStatusField to confirmationStatus2.toString(),
                confirmationCodeField to confirmationCode2
        ))
        registrationConfirmationCollection!!.insertMany(mutableListOf(
                user1Doc,
                user2Doc
        ))

    }

    @Test
    fun testFindAll() {
        val all = repository!!.find()
        assertThat(all.size, `is`(2))
        assertThat(all[0], `is`(expected1))
        assertThat(all[1], `is`(expected2))
    }

    @Test
    fun testFindByUserId() {
        assertThat(repository!!.find(
                userId = userId1.toString()
        ), `is`(Optional.of(expected1)))
    }

    @Test
    fun testFindByConfirmationStatus() {
        assertThat(repository!!.find(
                status = UNCONFIRMED
        ), `is`(listOf(expected1)))
    }

    @Test
    fun testDeleteById() {
        assertThat(repository!!.find(
                userId = userId1.toString()
        ), `is`(Optional.of(expected1)))
        repository!!.delete(userId1.toString())
        assertThat(repository!!.find(
                userId = userId1.toString()
        ), `is`(Optional.empty()))
    }

    @Test
    fun testBasicCreate() {
        val userId3 = ObjectId.get().toString()
        val confirmationStatus3 = CONFIRMED
        val createdDateTime3 = now()
        val confirmationCode3 = UUID.randomUUID().toString()
        val toCreate = RegistrationConfirmation(
                userId = userId3,
                confirmationCode = confirmationCode3,
                createdDateTime = createdDateTime3,
                confirmationStatus = confirmationStatus3
        )
        val created = repository!!.create(toCreate)

        assertEquals(Result.Success(toCreate), created)

        val document = registrationConfirmationCollection!!.find(BasicDBObject().append(userIdField, ObjectId(userId3))).first()
        assertThat(document.getObjectId(userIdField), `is`(ObjectId(userId3)))
        assertThat(document.getString(confirmationCodeField), `is`(confirmationCode3))
        assertThat(document.getLong(createdDateTimeField), `is`(createdDateTime3.toEpochMilli()))
        assertThat(document.getString(confirmationStatusField), `is`(CONFIRMED.name))

    }

    @Test
    fun testDuplicateWriteIsNotAllowed() {
        val result = repository!!.create(RegistrationConfirmation(
                userId = userId1.toString(),
                confirmationCode = confirmationCode2,
                createdDateTime = createdDateTime2,
                confirmationStatus = confirmationStatus2
        ))

        when (result) {
            is Result.Failure -> assertEquals(DUPLICATE_ID, result.errorValue.reason)
            else -> fail()
        }
    }
}
