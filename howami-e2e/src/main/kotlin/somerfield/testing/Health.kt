package somerfield.testing

import org.hamcrest.CustomMatcher
import org.hamcrest.Matcher
import java.net.URI

object Health {
    fun check(service: HealthCheckService): HealthResponse {
        println("Checking health at ${service.healthEndpoint()}")
        val response = HTTP.get(to = service.healthEndpoint())
        return if (response.status == 200 && response.json.getJSONObject("basic")?.getBoolean("healthy") == true) {
            Healthy
        } else {
            val message = response.json.getJSONObject("basic")?.getString("message") ?: "not provided"
            Unhealthy("Response code was '${response.status}'. Message was '${message}'")
        }
    }
}

interface HealthCheckService {
    fun healthEndpoint() : URI
}

object Matchers {
    fun healthy(): Matcher<HealthResponse> {
        return object : CustomMatcher<HealthResponse>("healthy") {
            override fun matches(item: Any?): Boolean {
                return when (item) {
                    Healthy -> true
                    else -> false
                }
            }
        }
    }
}

sealed class HealthResponse

object Healthy : HealthResponse() {
    override fun toString() = "Healthy"
}

data class Unhealthy(private val reason: String) : HealthResponse() {
    override fun toString() = "Unhealthy for reason '$reason'"
}