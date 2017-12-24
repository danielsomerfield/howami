package somerfield.howamiservice

import com.codahale.metrics.health.HealthCheck
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import somerfield.howamiservice.wire.JSON
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.configuration.EnvironmentVariableSubstitutor
import io.dropwizard.configuration.SubstitutingSourceProvider
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.bson.Document
import somerfield.howamiservice.domain.UserRegistrationService
import somerfield.howamiservice.repositories.UserAccountRepository
import somerfield.howamiservice.resources.RegistrationConfirmationResource
import somerfield.howamiservice.domain.RegistrationConfirmationService
import somerfield.howamiservice.resources.UserRegistrationResource

class HowAmIServiceApplication : Application<OrderServiceConfiguration>() {

    override fun run(configuration: OrderServiceConfiguration, environment: Environment) {


        environment.healthChecks().register("basic", object : HealthCheck() {
            override fun check(): Result {
                return Result.healthy()
            }
        })

        val binding = OrderServiceBinding.new(configuration)
        environment.jersey().register(binding.userRegistrationResource())
        environment.jersey().register(binding.registrationConfirmationResource())
        JSON.configureObjectMapper(environment.objectMapper)
    }

    override fun initialize(bootstrap: Bootstrap<OrderServiceConfiguration>) {
        bootstrap.configurationSourceProvider = SubstitutingSourceProvider(
                bootstrap.configurationSourceProvider,
                EnvironmentVariableSubstitutor()
        )
    }
}

fun main(args: Array<String>) {
    val arguments = if (args.isEmpty()) arrayOf("server") else args
    HowAmIServiceApplication().run(*arguments)
}

data class OrderServiceConfiguration
@JsonCreator
constructor(
        @JsonProperty("mongoHost")
        private val mongoHost: String?,
        @JsonProperty("mongoDatabase")
        private val mongoDatabase: String?
) : Configuration() {

    constructor() : this(null, null)

    fun getMongoHost(): String {
        return mongoHost ?: "localhost"
    }

    fun getMongoDatabase(): String {
        return mongoHost ?: "howami"
    }
}

/**
 * Low tech injection
 */
class OrderServiceBinding(private val configuration: OrderServiceConfiguration) {

    fun registrationConfirmationResource() = RegistrationConfirmationResource(registrationConfirmationService())

    private fun registrationConfirmationService(): RegistrationConfirmationService {
        return RegistrationConfirmationService()
    }

    fun userRegistrationResource() = UserRegistrationResource(
            userRegistrationService = userRegistrationService()
    )

    private fun userRegistrationService() = UserRegistrationService(
            userAccountRepository = userAccountRepository(),
            hashPassword = hashPasswordFn()
    )

    private fun hashPasswordFn() = { password: String -> password } //TODO: implement scrypt-based hashing
    private fun mongoDatabase(): MongoDatabase = MongoClient(configuration.getMongoHost()).getDatabase(configuration.getMongoDatabase())
    private fun userAccountCollection(): MongoCollection<Document> = mongoDatabase().getCollection("user_account")

    private fun userAccountRepository() = UserAccountRepository(userAccountCollection())
    companion object {
        fun new(orderServiceConfiguration: OrderServiceConfiguration) = OrderServiceBinding(orderServiceConfiguration)

    }
}