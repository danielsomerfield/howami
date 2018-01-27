package somerfield.howamiservice.domain.accounts

import somerfield.howamiservice.domain.ErrorCode
import somerfield.howamiservice.domain.ErrorResult
import somerfield.howamiservice.domain.Result
import somerfield.howamiservice.domain.accounts.EmailAddress.Companion.fromString

data class EmailAddress(private val emailText: String) {

    companion object {
        private val emailRegex = Regex("^[a-zA-Z0-9!#\$%&'*+-/=?^_`{|}~.]+@[a-zA-Z0-9-.]+\\.[a-zA-Z0-9-.]+\$", RegexOption.MULTILINE)

        fun fromString(string: String): Result<EmailAddress, ParseFailure> {
            return if (emailRegex.containsMatchIn(string)) {
                Result.Success(EmailAddress(string))
            }
            else {
                Result.Failure(ParseFailure("Couldn't parse $string"))
            }
        }
    }

    override fun toString(): String {
        return emailText
    }
}

fun String.toEmailAddress() = fromString(this)

data class ParseFailure(override val message: String) : ErrorResult