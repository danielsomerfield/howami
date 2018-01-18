package somerfield.howami.commsservice.wire

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventWireType<out T>
constructor(
        @JsonProperty("body")
        val body: T
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NotificationSentEventWireType
constructor(
        @JsonProperty("user-id")
        val userId: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NotificationFailedEventWireType
constructor(
        @JsonProperty("user-id")
        val userId: String,
        @JsonProperty("error-message")
        val message: String
)