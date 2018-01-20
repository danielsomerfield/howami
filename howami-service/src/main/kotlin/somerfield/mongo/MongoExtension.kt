package somerfield.mongo

import com.mongodb.BasicDBObject

fun <T> BasicDBObject.appendIfNotNull(field: String, value: T?): BasicDBObject {
    value?.let { this.append(field, value) }
    return this
}

fun <T, U> BasicDBObject.appendIfNotNull(field: String, value: T?, converter: (T) -> U): BasicDBObject {
    value?.let { this.append(field, converter(value)) }
    return this
}