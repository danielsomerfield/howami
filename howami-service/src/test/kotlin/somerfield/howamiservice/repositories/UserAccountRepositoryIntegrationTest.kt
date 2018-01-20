package somerfield.howamiservice.repositories

import com.github.fakemongo.Fongo
import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import org.bson.Document
import org.bson.types.ObjectId
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import somerfield.howamiservice.domain.accounts.AccountState
import somerfield.howamiservice.domain.accounts.UserAccount
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
        val id = repository?.create(UserAccount(username, passwordHash, emailAddress, state))
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
                emailAddress = emailAddress,
                state = state
        ))))
    }


}