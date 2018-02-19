package somerfield.howamiservice.domain

import somerfield.howamiservice.Env.commsServiceHost
import somerfield.howamiservice.Env.commsServiceHealthPort
import somerfield.testing.HealthCheckService
import java.net.URI

object CommsServiceClient : HealthCheckService {

    override fun healthEndpoint(): URI {
        return URI.create("${getServiceProto()}://${getServiceHost()}:${getHealthPort()}/healthcheck")
    }

    private fun getServiceHost(): String {
        return commsServiceHost
    }

    private fun getServiceProto(): String {
        return "http"
    }

    private fun getHealthPort(): Int {
        return commsServiceHealthPort
    }

}