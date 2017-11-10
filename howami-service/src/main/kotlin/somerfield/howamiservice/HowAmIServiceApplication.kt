package somerfield.howamiservice

import com.codahale.metrics.health.HealthCheck
import somerfield.howamiservice.wire.JSON
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Environment
import somerfield.howamiservice.resources.UserRegistrationResource

class HowAmIServiceApplication : Application<OrderServiceConfiguration>() {

    override fun run(configuration: OrderServiceConfiguration, environment: Environment) {
        environment.healthChecks().register("basic", object: HealthCheck(){
            override fun check(): Result {
                return Result.healthy()
            }
        })

//        val service = HowAmIService()
//        environment.jersey().register(OrderResource(service))
        environment.jersey().register(UserRegistrationResource())
        JSON.configureObjectMapper(environment.objectMapper)
    }
}

fun main(args: Array<String>) {
    val arguments = if (args.isEmpty()) arrayOf("server") else args
    HowAmIServiceApplication().run(*arguments)
}

class OrderServiceConfiguration : Configuration()