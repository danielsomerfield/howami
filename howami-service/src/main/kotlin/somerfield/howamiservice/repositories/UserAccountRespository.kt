package somerfield.howamiservice.repositories

import com.mongodb.BasicDBObject
import com.mongodb.DuplicateKeyException
import com.mongodb.MongoWriteException
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import org.bson.Document
import org.bson.types.ObjectId
import somerfield.howamiservice.domain.accounts.AccountState
import somerfield.howamiservice.domain.accounts.UserAccount
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
                    .append(emailAddressField, userAccount.emailAddress)
                    .append(stateField, userAccount.state.name)
            )
            CreateSuccess(document.getObjectId(idField).toString())
        } catch (e: MongoWriteException) {
            when (e.code) {
                11000 -> getDuplicateField(userAccount)
                        .map<CreateResult> { field -> DuplicateKeyError(field) }
                        .orElseGet { UnexpectedError(e.code, e.message ?: "An unexpected error has occurred") }
                else -> UnexpectedError(e.code, e.message ?: "An unexpected error has occurred")
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
            UserAccount(
                    username = doc.getString(usernameField),
                    passwordHash = doc.getString(passwordHashField),
                    emailAddress = doc.getString(emailAddressField),
                    state = AccountState.valueOf(doc.getString(stateField))
            )
        }
    }

    fun findByEmailAddress(
            emailAddress: String
    ): Optional<UserAccount> {
        return Optional.ofNullable(userAccountCollection.find(BasicDBObject()
                .append(emailAddressField, emailAddress))
                .first()).map { doc ->
            UserAccount(
                    username = doc.getString(usernameField),
                    passwordHash = doc.getString(passwordHashField),
                    emailAddress = doc.getString(emailAddressField),
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

interface CreateError

data class CreateSuccess(val id: String) : CreateResult()
data class DuplicateKeyError(val duplicateField: String) : CreateResult()
data class UnexpectedError(val code: Int, val message: String) : CreateResult()
