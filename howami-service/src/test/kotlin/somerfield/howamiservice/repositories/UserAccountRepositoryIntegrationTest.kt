package somerfield.howamiservice.repositories

import com.github.fakemongo.Fongo
import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import junit.framework.AssertionFailedError
import org.bson.Document
import org.bson.types.ObjectId
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.experimental.categories.Category
import somerfield.howamiservice.domain.accounts.AccountState
import somerfield.howamiservice.domain.accounts.UserAccount
import somerfield.howamiservice.domain.accounts.toEmailAddress
import somerfield.testing.IntegrationTests
import java.util.*

@Category(IntegrationTests::class)
class UserAccountRepositoryIntegrationTest {

    private val databaseName = "howami"

    private val mongo = Fongo("mock-mongo")

    private var repository: UserAccountRepository? = null

    private var userAccountCollection: MongoCollection<Document>? = null

    @Before
    fun setup() {
        mongo.dropDatabase(databaseName)
        userAccountCollection = mongo.getDatabase(databaseName).getCollection("user_account")

        repository = UserAccountRepository(userAccountCollection!!)
    }

    @Test
    fun createRecordInDataStore() {
        val username = "username1"
        val passwordHash = "password1Hash"
        val emailAddress = "foo@example.com"
        val state = AccountState.PENDING
        val createResult = repository?.create(UserAccount(username, passwordHash,
                emailAddress.toEmailAddress().getUnsafe(), state))
        val id = when (createResult) {
            is CreateSuccess -> createResult.id
            else -> throw AssertionError("Create failed.")
        }
        val userAccounts = userAccountCollection!!.find(BasicDBObject("_id", ObjectId(id)))
        assertThat(userAccounts.first(), `is`(Document(mapOf(
                "_id" to ObjectId(id),
                "username" to username,
                "password_hash" to passwordHash,
                "email_address" to emailAddress,
                "state" to "PENDING"
        ))))
    }

    @Test
    fun findRecordInDataStore() {
        val username = "username2"
        val passwordHash = "password2Hash"
        val emailAddress = "fo2@example.com"
        val state = AccountState.PENDING
        userAccountCollection!!.insertOne(Document(mapOf(
                "username" to username,
                "password_hash" to passwordHash,
                "email_address" to emailAddress,
                "state" to "PENDING"
        )))

        assertThat(repository!!.findByUsername(username), `is`(Optional.of(UserAccount(
                username = username,
                passwordHash = passwordHash,
                emailAddress = emailAddress.toEmailAddress().getUnsafe(),
                state = state
        ))))
    }

    @Test
    fun createDuplicateUsernameReturnsFailure() {
        val username = "username2"
        val passwordHash = "password2Hash"
        val emailAddress = "fo2@example.com"
        val state = AccountState.PENDING
        userAccountCollection!!.insertOne(Document(mapOf(
                "username" to username,
                "password_hash" to passwordHash,
                "email_address" to emailAddress,
                "state" to "PENDING"
        )))

        val result = repository?.create(UserAccount(username, passwordHash, "test@example.com".toEmailAddress().getUnsafe(), state))
        when (result) {
            is DuplicateKeyError -> assertThat(result, `is`(DuplicateKeyError("username")))
            else -> throw AssertionFailedError("Should have been duplicate key error but was $result")
        }
    }

    @Test
    fun createDuplicateEmailReturnsFailure() {
        val username = "username2"
        val passwordHash = "password2Hash"
        val emailAddress = "fo2@example.com"
        val state = AccountState.PENDING
        userAccountCollection!!.insertOne(Document(mapOf(
                "username" to username,
                "password_hash" to passwordHash,
                "email_address" to emailAddress.toString(),
                "state" to "PENDING"
        )))

        val result = repository?.create(UserAccount("uname1", passwordHash, emailAddress.toEmailAddress().getUnsafe(), state))
        when (result) {
            is DuplicateKeyError -> assertThat(result, `is`(DuplicateKeyError("emailAddress")))
            else -> throw AssertionFailedError("Should have been duplicate key error but was $result")
        }
    }
}