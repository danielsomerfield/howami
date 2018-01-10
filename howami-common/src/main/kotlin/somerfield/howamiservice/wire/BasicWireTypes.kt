package somerfield.howamiservice.wire

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommandWireType<out T> constructor(
        @JsonProperty("body") val body: T)

sealed class ResponseWireType<T> {
    abstract val header: CommandResponseHeaderWireType
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommandResponseWireType<T>(
        override val header: CommandResponseHeaderWireType,
        val body: T
) : ResponseWireType<T>()

@JsonIgnoreProperties(ignoreUnknown = true)
data class ErrorResponseWireType constructor(
        @JsonProperty("header")
        override val header: CommandResponseHeaderWireType,
        @JsonProperty("error-code")
        val errorCode: String,
        @JsonProperty("error-message")
        val errorMessage: String
) : ResponseWireType<Nothing>()

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommandResponseHeaderWireType
constructor(
        @JsonProperty("request-id") val requestId: String)