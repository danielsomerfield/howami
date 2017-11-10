package somerfield.testing

import junit.framework.AssertionFailedError

object Async {
    fun <T> responseOf(fn: () -> T): CommandResponse<T> {
        return try {
            CommandResponse.DataResponse(fn())
        } catch (e: Exception) {
            CommandResponse.ExceptionResponse(e)
        }
    }

    fun <T> waitFor(fn: () -> CommandResponse<T>, timeoutInSeconds: Long = 10): Wait<T> {
        return Wait(fn, timeoutInSeconds * 1000)
    }

    sealed class CommandResponse<out T> {
        data class DataResponse<out T>(val result: T) : CommandResponse<T>() {
        }
        data class FailedResponse<out T>(val errorCode: String) : CommandResponse<T>() {
        }
        data class ExceptionResponse<out T>(val e: Exception) : CommandResponse<T>() {
        }
    }

    data class WaitResponse<T>(val result: CommandResponse<T>) {
        fun <Y> then(thenFn: (T) -> Y): WaitResponse<Y> {
            TODO()
        }
    }

    class Wait<T>(
            private val fn: () -> CommandResponse<T>,
            private val timeoutInMillis: Long
    ) {

        fun condition(checkFn: (CommandResponse<T>) -> Boolean): WaitResponse<T> {
            val startTime = System.currentTimeMillis()

            var ex: Exception? = null
            var response: CommandResponse<T>? = null
            while (System.currentTimeMillis() - startTime < timeoutInMillis) {
                try {
                    response = fn()
                    if (checkFn(response)) {
                        return WaitResponse(response)
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

        fun toBe(expected: T): WaitResponse<T> {
            return condition {
                when (it) {
                    is CommandResponse.DataResponse -> it.result == expected
                    else -> false
                }
            }
        }


        fun toExist(): WaitResponse<T> {
            return condition {
                when (it) {
                    is CommandResponse.DataResponse -> true
                    else -> false
                }
            }
        }


    }
}