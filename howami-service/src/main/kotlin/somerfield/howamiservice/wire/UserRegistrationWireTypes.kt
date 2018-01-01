package somerfield.howamiservice.wire

import com.fasterxml.jackson.annotation.JsonProperty

data class UserRegistrationWireTypes
constructor(
        @JsonProperty("username") val username: String,
        @JsonProperty("password") val password: String,
        @JsonProperty("email") val email: String
)

data class UserRegistrationResponseWireType
constructor(
        @JsonProperty("user-id") val userId: String
)

data class RegistrationConfirmationWireType
constructor(
        @JsonProperty("email")
        val email: String,

        @JsonProperty("user-id")
        val userId: String,

        @JsonProperty("confirmation-code")
        val confirmationCode: String,

        @JsonProperty("created-datetime")
        val createdDateTime: String,

        @JsonProperty("confirmation-status")
        val confirmationStatus: String
)

data class ConfirmationResponseWireType
constructor(
    @JsonProperty("result")
    val result: String,
    val message: String
)