package somerfield.howamiservice.repositories

import com.mongodb.client.MongoCollection
import org.bson.Document
import somerfield.howamiservice.domain.UserAccount

class UserAccountRepository(private val userAccountCollection: MongoCollection<Document>) {
    fun create(userAccount: UserAccount): String {
        val document = Document()
        userAccountCollection.insertOne(document
                .append("username", userAccount.username)
                .append("password_hash", userAccount.passwordHash)
                .append("email_address", userAccount.emailAddress)
                .append("state", userAccount.state.name)
        )
        return document.getObjectId("_id").toString()
    }
}