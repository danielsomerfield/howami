package somerfield.howamiservice.wire

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONObject
import org.junit.Test
import java.util.*

class WireTypesTest {

    val objectMapper = JSON.configureObjectMapper(ObjectMapper())

    @Test
    fun deserializeCommand() {
        val message = JSONObject()
                .put("header", JSONObject()
                        .put("request-id", UUID.randomUUID().toString())
                )
                .put("body", JSONObject()
                        .put("username", "unane")
                        .put("password", "pwd")
                        .put("phone-number", "1-555-1212")
                ).toString()
        val userRegistrationCommand: CommandWireType<UserRegistrationCommandWireType> = objectMapper.readValue(message, object : TypeReference<CommandWireType<UserRegistrationCommandWireType>>() {})
    }
}