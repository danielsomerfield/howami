package somerfield.howamiservice

import com.codahale.metrics.health.HealthCheck
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import somerfield.howamiservice.wire.JSON
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Environment
import org.bson.Document
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

        val configuration = OrderServiceBinding.new(configuration)
        environment.jersey().register(configuration.userRegistrationResource())
        JSON.configureObjectMapper(environment.objectMapper)
    }
}

fun main(args: Array<String>) {
    val arguments = if (args.isEmpty()) arrayOf("server") else args
    HowAmIServiceApplication().run(*arguments)
}

data class OrderServiceConfiguration(
        val mongoHost: String = "mongo",
        val mongoDatabase: String = "howami"
) : Configuration()

class OrderServiceBinding(private val configuration: OrderServiceConfiguration) {

    fun userRegistrationResource() = UserRegistrationResource(
            userRegistrationService = userRegistrationService()
    )

    private fun userRegistrationService() = UserRegistrationService(
            userAccountRepository = userAccountRepository(),
            hashPassword = hashPasswordFn()
    )

    private fun hashPasswordFn() = { password: String -> password } //TODO: implement scrypt-based hashing

    private fun mongoDatabase(): MongoDatabase = MongoClient(configuration.mongoHost).getDatabase(configuration.mongoDatabase)
    private fun userAccountCollection(): MongoCollection<Document> = mongoDatabase().getCollection("user_account")
    private fun userAccountRepository() = UserAccountRepository(userAccountCollection())

    companion object {
        fun new(orderServiceConfiguration: OrderServiceConfiguration) = OrderServiceBinding(orderServiceConfiguration)
    }
}