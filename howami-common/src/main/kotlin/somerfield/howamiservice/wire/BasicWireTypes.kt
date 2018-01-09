package somerfield.howamiservice.wire

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommandWireType<out T> constructor(
        @JsonProperty("body") val body: T)

sealed class ResponseWireType<T> {
    abstract val header: CommandResponseHeaderWireType
}

data class CommandResponseWireType<T>(
        override val header: CommandResponseHeaderWireType,
        val body: T
) : ResponseWireType<T>()

data class ErrorResponseWireType (
        override val header: CommandResponseHeaderWireType,
        val errorCode: String,
        val errorMessage: String
) : ResponseWireType<Nothing>()

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommandResponseHeaderWireType
constructor(
        @JsonProperty("request-id") val requestId: String)