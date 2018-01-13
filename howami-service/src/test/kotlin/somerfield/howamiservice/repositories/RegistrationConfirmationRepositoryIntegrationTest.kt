package somerfield.howamiservice.repositories

import com.github.fakemongo.Fongo
import com.mongodb.client.MongoCollection
import org.bson.Document
import org.bson.types.ObjectId
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import somerfield.howamiservice.domain.ConfirmationStatus
import somerfield.howamiservice.domain.RegistrationConfirmation
import somerfield.testing.IntegrationTests
import java.time.Instant.now
import java.util.*

@Category(IntegrationTests::class)
class RegistrationConfirmationRepositoryIntegrationTest {


    private val emailField = "email"
    private val userIdField = "_id"
    private val createdDateTimeField = "created_datetime"
    private val confirmationStatusField = "confirmation_status"
    private val confirmationCodeField = "confirmation_code"

    private val databaseName = "howami"

    private val mongo = Fongo("mock-mongo")

    private var repository: RegistrationConfirmationRepository? = null

    private var registrationConfirmationCollection: MongoCollection<Document>? = null

    private val userId1 = ObjectId.get()
    private val email1 = "test@example.com"
    private val createdDateTime1 = now()
    private val confirmationStatus1 = ConfirmationStatus.UNCONFIRMED
    private val confirmationCode1 = UUID.randomUUID().toString()

    private val userId2 = ObjectId.get()
    private val email2 = "test@example.com"
    private val createdDateTime2 = now()
    private val confirmationStatus2 = ConfirmationStatus.CONFIRMED
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
                emailField to email1,
                createdDateTimeField to createdDateTime1.toEpochMilli(),
                confirmationStatusField to confirmationStatus1.toString(),
                confirmationCodeField to confirmationCode1
        ))
        val user2Doc = Document(mapOf(
                userIdField to userId2,
                emailField to email2,
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
                status = ConfirmationStatus.UNCONFIRMED
        ), `is`(listOf(expected1)))
    }

}