package somerfield.howamiservice.domain

import somerfield.testing.HealthCheckService
import java.net.URI

object CommsServiceClient : HealthCheckService {

    override fun healthEndpoint(): URI {
        return URI.create("${getServiceProto()}://${getServiceHost()}:${getHealthPort()}/healthcheck")
    }

    private fun getServiceHost(): String {
        return System.getenv().getOrDefault("COMMS_SERVICE_SERVICE_HOST", "http://localhost")
    }

    private fun getServiceProto(): String {
        return "http"
    }

    private fun getHealthPort(): Int {
        return System.getenv().getOrDefault("COMMS_SERVICE_SERVICE_PORT_HEALTH", "8081").toInt()
    }

}