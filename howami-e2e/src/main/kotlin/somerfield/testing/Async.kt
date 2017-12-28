package somerfield.testing

import junit.framework.AssertionFailedError
import java.util.*

object Async {
    fun <T> responseOf(fn: () -> T?): () -> CommandResponse<T> {
        return {
            try {
                val result = fn()
                if (result == null) CommandResponse.EmptyResponse() else CommandResponse.DataResponse(result)
            } catch (e: Exception) {
                CommandResponse.ExceptionResponse(e)
            }
        }
    }

    fun <T> responseOfOptional(fn: () -> Optional<T>): () -> CommandResponse<T> {
        return {
            try {
                val result = fn()
                if (result.isPresent) {
                    CommandResponse.DataResponse(result.get())
                } else {
                    CommandResponse.EmptyResponse()
                }
            } catch (e: Exception) {
                CommandResponse.ExceptionResponse(e)
            }
        }
    }

    fun <T> waitFor(fn: () -> CommandResponse<T>, testFn: (CommandResponse<T>) -> Boolean, timeoutInSeconds: Long = 10): Wait<T> {
        val timeoutInMillis = timeoutInSeconds * 1000
        val startTime = System.currentTimeMillis()

        var ex: Exception? = null
        var response: CommandResponse<T>? = null
        while (System.currentTimeMillis() - startTime < timeoutInMillis) {
            try {
                //TODO: make this call on a cancelable executor so it can't hang
                response = fn()
                if (testFn(response)) {
                    return Wait(response)
                }
                Thread.sleep(500)
            } catch (e: Exception) {
                ex = e
            }
        }

        throw if (ex == null) AssertionFailedError("Timed out waiting for condition. Last response was $response") else {
            AssertionFailedError("Timed out waiting for condition. Exception was: $ex")
        }

    }

    sealed class CommandResponse<in T> {
        data class DataResponse<T>(val result: T) : CommandResponse<T>()
        //        data class FailedResponse<T>(val errorCode: String) : CommandResponse<Nothing>()
        data class ExceptionResponse<in T>(val e: Exception) : CommandResponse<T>()
        class EmptyResponse<in T> : CommandResponse<T>() {
            override fun toString() = "<empty response>"
        }
    }

    class Wait<T>(private val response: CommandResponse<T>) {
        fun then(thenFn: (CommandResponse<T>) -> Unit) {
            thenFn(response)
        }
    }

}

fun <T> toBe(expected: T): (Async.CommandResponse<T>) -> Boolean {
    return { response ->
        when (response) {
            is Async.CommandResponse.DataResponse<*> -> response.result == expected
            else -> false
        }
    }
}

fun <T> toExist(): (Async.CommandResponse<T>) -> Boolean {
    return { response ->
        when (response) {
            is Async.CommandResponse.DataResponse -> true
            else -> false
        }
    }
}