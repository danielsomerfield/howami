package somerfield.resources

import java.util.*

class RequestIdSource {
    fun getOrCreate() = UUID.randomUUID().toString()
}