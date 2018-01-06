package somerfield.howamiservice.wire

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JSON {
    fun configureObjectMapper(objectMapper: ObjectMapper): ObjectMapper {
        objectMapper.registerKotlinModule()
        return objectMapper
    }
}