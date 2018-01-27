package somerfield.howamiservice.repositories

import com.mongodb.BasicDBObject
import com.mongodb.MongoWriteException
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import org.bson.Document
import org.bson.types.ObjectId
import somerfield.howamiservice.domain.accounts.AccountState
import somerfield.howamiservice.domain.accounts.EmailAddress
import somerfield.howamiservice.domain.accounts.UserAccount
import somerfield.howamiservice.domain.admin.NeedInterventionException
import java.util.*

class UserAccountRepository(private val userAccountCollection: MongoCollection<Document>) {
    private val passwordHashField = "password_hash"
    private val usernameField: String = "username"
    private val emailAddressField = "email_address"
    private val stateField = "state"
    private val idField = "_id"

    init {
        userAccountCollection.createIndex(Indexes.ascending(usernameField), IndexOptions().unique(true))
        userAccountCollection.createIndex(Indexes.ascending(emailAddressField), IndexOptions().unique(true))
    }

    fun create(userAccount: UserAccount): CreateResult {
        val document = Document()
        return try {
            userAccountCollection.insertOne(document
                    .append(usernameField, userAccount.username)
                    .append(passwordHashField, userAccount.passwordHash)
                    .append(emailAddressField, userAccount.emailAddress.toString())
                    .append(stateField, userAccount.state.name)
            )
            CreateSuccess(document.getObjectId(idField).toString())
        } catch (e: MongoWriteException) {
            when (e.code) {
                11000 -> getDuplicateField(userAccount)
                        .map<CreateResult> { field -> DuplicateKeyError(field) }
                        .orElseGet { UnexpectedDBError(e.code, e.message ?: "An unexpected error has occurred") }
                else -> UnexpectedDBError(e.code, e.message ?: "An unexpected error has occurred")
            }
        }
    }

    private fun getDuplicateField(userAccount: UserAccount): Optional<String> {
        return when {
            findByUsername(username = userAccount.username).isPresent -> Optional.of(UserAccount::username.name)
            findByEmailAddress(emailAddress = userAccount.emailAddress).isPresent -> Optional.of(UserAccount::emailAddress.name)
            else -> Optional.empty()
        }
    }

    fun findByUsername(
            username: String
    ): Optional<UserAccount> {
        return Optional.ofNullable(userAccountCollection.find(BasicDBObject()
                .append(usernameField, username))
                .first()).map { doc ->
            val emailAddress = EmailAddress.fromString(doc.getString(emailAddressField))

            UserAccount(
                    username = doc.getString(usernameField),
                    passwordHash = doc.getString(passwordHashField),
                    emailAddress = emailAddress.getOrThrow(NeedInterventionException("Invalid email address in the database. Fix now!")),
                    state = AccountState.valueOf(doc.getString(stateField))
            )
        }
    }

    fun findByEmailAddress(
            emailAddress: EmailAddress
    ): Optional<UserAccount> {
        return Optional.ofNullable(userAccountCollection.find(BasicDBObject()
                .append(emailAddressField, emailAddress.toString()))
                .first()).map { doc ->
            UserAccount(
                    username = doc.getString(usernameField),
                    passwordHash = doc.getString(passwordHashField),
                    emailAddress = emailAddress,
                    state = AccountState.valueOf(doc.getString(stateField))
            )
        }
    }

    fun update(userId: String, state: AccountState): Boolean {
        return userAccountCollection.findOneAndUpdate(
                BasicDBObject().append(idField, ObjectId(userId)),
                BasicDBObject().append("\$set", BasicDBObject(stateField, state.name))) != null
    }
}

sealed class CreateResult {

}

data class CreateSuccess(val id: String) : CreateResult()
data class DuplicateKeyError(val duplicateField: String) : CreateResult()

//TODO: This should be replaced with retry
data class UnexpectedDBError(val code: Int, val message: String) : CreateResult()
