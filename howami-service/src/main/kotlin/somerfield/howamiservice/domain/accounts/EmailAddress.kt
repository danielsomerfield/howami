package somerfield.howamiservice.domain.accounts

import somerfield.howamiservice.domain.ErrorResult
import somerfield.howamiservice.domain.Result
import somerfield.howamiservice.domain.accounts.EmailAddress.Companion.fromString

data class EmailAddress(val emailText: String) {

    companion object {
        fun fromString(string: String): Result<EmailAddress, ErrorResult> {
            //TODO: validation
            return Result.doTry { EmailAddress(string) }
        }
    }
}

fun String.toEmailAddress() = fromString(this)