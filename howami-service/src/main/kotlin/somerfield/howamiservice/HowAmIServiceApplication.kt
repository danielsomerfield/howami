package somerfield.howamiservice

import com.codahale.metrics.health.HealthCheck
import somerfield.howamiservice.wire.JSON
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Environment
import somerfield.howamiservice.domain.UserRegistrationService
import somerfield.howamiservice.repositories.UserAccountRepository
import somerfield.howamiservice.resources.UserRegistrationResource

class HowAmIServiceApplication : Application<OrderServiceConfiguration>() {

    override fun run(configuration: OrderServiceConfiguration, environment: Environment) {
        environment.healthChecks().register("basic", object : HealthCheck() {
            override fun check(): Result {
                return Result.healthy()
            }
        })

        environment.jersey().register(OrderServiceBinding.userRegistrationResource)
        JSON.configureObjectMapper(environment.objectMapper)
    }
}

fun main(args: Array<String>) {
    val arguments = if (args.isEmpty()) arrayOf("server") else args
    HowAmIServiceApplication().run(*arguments)
}

class OrderServiceConfiguration : Configuration()

object OrderServiceBinding {

    val userRegistrationResource: UserRegistrationResource = userRegistrationResource()

    private fun userRegistrationResource() = UserRegistrationResource(
            userRegistrationService = OrderServiceBinding.userRegistrationService()
    )

    private fun userRegistrationService() = UserRegistrationService(
            userAccountRepository = userAccountRepository(),
            hashPassword = hashPasswordFn()
    )

    private fun hashPasswordFn() = { password: String -> password } //TODO: implement scrypt-based hashing

    private fun userAccountRepository() = UserAccountRepository()
}