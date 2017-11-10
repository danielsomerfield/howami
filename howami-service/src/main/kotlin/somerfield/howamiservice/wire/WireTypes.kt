package somerfield.howamiservice.wire

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule


data class CommandHeaderWireType
constructor(
        @JsonProperty("request-id") val requestId: String
)

data class CommandWireType<out T> constructor(
        val header: CommandHeaderWireType, @JsonProperty("body") val body: T)

data class CommandResponseWireType<T>(
        val header: CommandResponseHeaderWireType,
        val body: T
)

data class CommandResponseHeaderWireType
    constructor(
            @JsonProperty("request-id") val requestId: String)

data class UserRegistrationWireType
constructor(
        @JsonProperty("username") val username: String,
        @JsonProperty("password") val password: String,
        @JsonProperty("phone-number") val phoneNumber: String
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