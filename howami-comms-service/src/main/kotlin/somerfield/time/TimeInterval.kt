package somerfield.time

data class TimeInterval(private val millis: Long) {
    fun inMillis() = millis;

    infix operator fun times(number: Number): TimeInterval {
        return TimeInterval(number.toLong() * this.millis)
    }

    fun inSeconds(): Long = millis / 1000L

}

fun Number.seconds(): TimeInterval {
    return TimeInterval(this.toLong() * 1000)
}

fun Number.minute(): TimeInterval {
    return this.minutes()
}

fun Number.minutes(): TimeInterval {
    return this.seconds() * 60L
}