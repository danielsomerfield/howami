package somerfield.howamiservice.wire

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers.`is`
import org.json.JSONObject
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.*

class WireTypesTest {

    val objectMapper = JSON.configureObjectMapper(ObjectMapper())

    @Test
    fun deserializeCommand() {
        val id = UUID.randomUUID().toString()
        val message = JSONObject()
                .put("header", JSONObject()
                        .put("request-id", id)
                )
                .put("body", JSONObject()
                        .put("username", "uname")
                        .put("password", "pwd")
                        .put("email", "email@example.com")
                ).toString()
        val userRegistrationCommand: CommandWireType<UserRegistrationCommandWireType> = objectMapper.readValue(message, object : TypeReference<CommandWireType<UserRegistrationCommandWireType>>() {})
        assertThat(userRegistrationCommand.header.requestId, `is`(id))
        assertThat(userRegistrationCommand.body.username, `is`("uname"))
        assertThat(userRegistrationCommand.body.password, `is`("pwd"))
        assertThat(userRegistrationCommand.body.email, `is`("email@example.com"))
    }
}