package somerfield.howamiservice.repositories

import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import org.bson.Document
import somerfield.howamiservice.domain.AccountState
import somerfield.howamiservice.domain.UserAccount
import java.util.*

class UserAccountRepository(private val userAccountCollection: MongoCollection<Document>) {
    private val passwordHashField = "password_hash"
    private val usernameField: String = "username"
    private val emailAddressField = "email_address"
    private val stateField = "state"

    fun create(userAccount: UserAccount): String {
        val document = Document()
        userAccountCollection.insertOne(document
                .append(usernameField, userAccount.username)
                .append(passwordHashField, userAccount.passwordHash)
                .append(emailAddressField, userAccount.emailAddress)
                .append(stateField, userAccount.state.name)
        )
        return document.getObjectId("_id").toString()
    }

    fun find(username: String): Optional<UserAccount> {
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
}