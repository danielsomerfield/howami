package somerfield.howami.commsservice.wire

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserRegistrationEventWireType
constructor(
        @JsonProperty("user-id")
        val userId: String,
        @JsonProperty("email")
        val email: String,
        @JsonProperty("confirmation-code")
        val confirmationCode: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventWireType<out T>
constructor(
        @JsonProperty("body")
        val body: T
)