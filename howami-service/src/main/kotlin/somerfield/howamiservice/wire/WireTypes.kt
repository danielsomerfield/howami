package somerfield.howamiservice.wire

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule


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

data class CommandResponseHeaderWireType
    constructor(
            @JsonProperty("request-id") val requestId: String)

data class UserRegistrationCommandWireType
constructor(
        @JsonProperty("username") val username: String,
        @JsonProperty("password") val password: String,
        @JsonProperty("email") val email: String
)

data class UserRegistrationResponseWireType
constructor (
    @JsonProperty("user-id") val userID: String
)

object JSON {
    fun configureObjectMapper(objectMapper: ObjectMapper): ObjectMapper {
        objectMapper.registerKotlinModule()
        return objectMapper
    }
}