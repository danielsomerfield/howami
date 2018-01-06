package somerfield.howamiservice.domain

import somerfield.testing.HealthCheckService
import java.net.URI

object CommsServiceClient : HealthCheckService {

    private val serviceHost = System.getenv().getOrDefault("COMMS_SERVICE_BASE_URL", "http://localhost")

    override fun healthEndpoint(): URI {
        return URI.create("$serviceHost:${CommsServiceClient.getHealthPort()}/healthcheck")
    }

    private fun getHealthPort(): Int {
        return 8081
    }

}