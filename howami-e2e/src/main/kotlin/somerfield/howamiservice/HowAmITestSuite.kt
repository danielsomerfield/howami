package somerfield.howamiservice

import org.junit.runner.Description
import org.junit.runner.JUnitCore
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

fun main(args: Array<String>) {
    val core = JUnitCore()
    core.addListener(object : RunListener() {

        override fun testStarted(description: Description?) {
            println("Started test $description")
        }

        override fun testFailure(failure: Failure) {
            println("Failed test $failure")
        }
    })
    core.run(HowAmISmokeTest::class.java)
}