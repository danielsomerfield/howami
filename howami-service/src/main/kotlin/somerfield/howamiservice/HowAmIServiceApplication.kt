package somerfield.howamiservice

import com.codahale.metrics.health.HealthCheck
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import somerfield.howamiservice.wire.JSON
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.configuration.EnvironmentVariableSubstitutor
import io.dropwizard.configuration.SubstitutingSourceProvider
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import somerfield.howamiservice.domain.UserRegistrationService
import somerfield.howamiservice.repositories.UserAccountRepository
import somerfield.howamiservice.resources.RegistrationConfirmationResource
import somerfield.howamiservice.domain.RegistrationConfirmationService
import somerfield.howamiservice.repositories.RegistrationConfirmationRepository
import somerfield.howamiservice.resources.UserRegistrationResource
import somerfield.resources.RequestIdSource

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

    private val requestIdSource = RequestIdSource()

    private fun requestIdSource() = requestIdSource //TODO: This needs to be replaced with something that reads headers

    fun registrationConfirmationResource() = RegistrationConfirmationResource(registrationConfirmationService(), requestIdSource())

    private fun registrationConfirmationService(): RegistrationConfirmationService {
        return RegistrationConfirmationService(
                registrationConfirmationRepository = registrationConfirmationRepository()
        )
    }

    private fun registrationConfirmationRepository(): RegistrationConfirmationRepository {
        return RegistrationConfirmationRepository(mongoDatabase().getCollection("registration_confirmation"))
    }

    fun userRegistrationResource() = UserRegistrationResource(
            userRegistrationService = userRegistrationService(),
            requestIdSource = requestIdSource()
    )

    private fun userRegistrationService() = UserRegistrationService(
            userAccountRepository = userAccountRepository(),
            hashPassword = hashPasswordFn()
    )

    private fun hashPasswordFn() = { password: String -> password } //TODO: implement scrypt-based hashing

    private fun mongoDatabase(): MongoDatabase = MongoClient(configuration.getMongoHost()).getDatabase(configuration.getMongoDatabase())

    private fun userAccountRepository() = UserAccountRepository(mongoDatabase().getCollection("user_account"))

    companion object {
        fun new(orderServiceConfiguration: OrderServiceConfiguration) = OrderServiceBinding(orderServiceConfiguration)

    }
}