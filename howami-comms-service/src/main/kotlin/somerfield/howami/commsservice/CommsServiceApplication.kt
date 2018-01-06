package somerfield.howami.commsservice

import com.codahale.metrics.health.HealthCheck
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Environment
import somerfield.howamiservice.wire.JSON

class CommsServiceApplication : Application<CommsServiceConfiguration>() {
    override fun run(configuration: CommsServiceConfiguration, environment: Environment) {

        environment.healthChecks().register("basic", object : HealthCheck() {
            override fun check(): Result {
                return Result.healthy()
            }
        })

        JSON.configureObjectMapper(environment.objectMapper)
    }

}

fun main(args: Array<String>) {
    val arguments = if (args.isEmpty()) arrayOf("server") else args
    CommsServiceApplication().run(*arguments)
}

class CommsServiceConfiguration : Configuration() {

}
