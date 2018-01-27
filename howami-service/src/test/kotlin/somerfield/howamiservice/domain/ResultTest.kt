package somerfield.howamiservice.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ResultTest {

    @Test
    fun flatMapSuccessToSuccess() {
        val result = TestFunctions.succeed1()
                .flatMap { _ -> TestFunctions.succeed2() }
        assertEquals(result, Result.Success(5))
    }

    @Test
    fun flatMapSuccessToFailure() {
        val result = Result.Success("5")
                .flatMap { _ -> TestFunctions.fail1() }
        assertEquals(result, Result.Failure(BazErrorResult("baz")))
    }

    @Test
    fun flatMapFailureToSuccess() {
        val result = TestFunctions.fail1()
                .flatMap { _ -> TestFunctions.succeed2() }
        assertEquals(result, Result.Failure(BazErrorResult("baz")))
    }

    @Test
    fun flatMapFailureToFailure() {
        val result = TestFunctions.fail1()
                .flatMap { _ -> TestFunctions.fail2() }
        assertEquals(result, Result.Failure(BazErrorResult("baz")))
    }

    @Test
    fun mapSuccessToSuccess() {
        assertEquals(TestFunctions.succeed1().map { "bar" }, Result.Success("bar"))
    }

    @Test
    fun mapFailureToSuccess() {
        assertEquals(TestFunctions.fail1().map { "bar" }, Result.Failure(BazErrorResult("baz")))
    }

}

object TestFunctions {
    fun succeed1(): Result<String, BasicErrorResult> = Result.Success("foo")
    fun succeed2(): Result<Int, BasicErrorResult> = Result.Success(5)
    fun fail1(): Result<String, BazErrorResult> = Result.Failure(BazErrorResult("baz"))
    fun fail2(): Result<String, BasicErrorResult> = Result.Failure(BasicErrorResult(ErrorCode.MALFORMED_FIELDS, "fail2"))
}

data class BazErrorResult(private val message: String) : ErrorResult {
    override fun message(): String {
        return message
    }

    override fun errorCode(): ErrorCode {
        return ErrorCode.MALFORMED_FIELDS
    }
}