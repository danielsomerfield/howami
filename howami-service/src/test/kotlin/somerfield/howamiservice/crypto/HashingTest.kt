package somerfield.howamiservice.crypto

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class HashingTest {

    private val value = UUID.randomUUID().toString()

    @Test
    fun testHashIsValidated() {
        val hash = Hashing.buildHash(value)
        assertThat(Hashing.validate(value, hash), `is`(true))
    }

    @Test
    fun testCorruptHashIsInvalid() {
        val hash = Hashing.buildHash(value)
        assertThat(Hashing.validate(value, hash + "1"), `is`(false))
    }
}