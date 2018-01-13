package somerfield.howamiservice.wire

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserRegistrationWireTypes
constructor(
        @JsonProperty("username") val username: String,
        @JsonProperty("password") val password: String,
        @JsonProperty("email") val email: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserRegistrationResponseWireType
constructor(
        @JsonProperty("user-id") val userId: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RegistrationConfirmationWireType
constructor(
        @JsonProperty("user-id")
        val userId: String,

        @JsonProperty("confirmation-code")
        val confirmationCode: String,

        @JsonProperty("created-datetime")
        val createdDateTime: String,

        @JsonProperty("confirmation-status")
        val confirmationStatus: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConfirmationResponseWireType
constructor(
        @JsonProperty("result")
        val result: String,
        val message: String
)