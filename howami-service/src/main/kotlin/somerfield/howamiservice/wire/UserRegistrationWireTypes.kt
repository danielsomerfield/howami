package somerfield.howamiservice.wire

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class UserRegistrationWireTypes
constructor(
        @JsonProperty("username") val username: String,
        @JsonProperty("password") val password: String,
        @JsonProperty("email") val email: String
)

data class UserRegistrationResponseWireType
constructor(
        @JsonProperty("user-id") val userID: String
)

data class RegistrationConfirmationWireType
constructor(
        @JsonProperty("email")
        val email: String,

        @JsonProperty("user-id")
        val userId: String,

        @JsonProperty("confirmation-code")
        val confirmationCode: String,

        @JsonProperty("created-date-time")
        val createdDateTime: Date,

        @JsonProperty("confirmation-status")
        val confirmationStatus: String
)
