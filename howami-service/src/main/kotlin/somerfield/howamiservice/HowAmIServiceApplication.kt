package somerfield.howamiservice

import com.codahale.metrics.health.HealthCheck
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.configuration.EnvironmentVariableSubstitutor
import io.dropwizard.configuration.SubstitutingSourceProvider
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.federecio.dropwizard.swagger.SwaggerBundle
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration
import org.apache.commons.text.RandomStringGenerator
import org.apache.kafka.clients.producer.KafkaProducer
import somerfield.howamiservice.domain.LoginService
import somerfield.howamiservice.domain.RegistrationConfirmationService
import somerfield.howamiservice.domain.UserEventProducer
import somerfield.howamiservice.domain.UserRegistrationService
import somerfield.howamiservice.repositories.RegistrationConfirmationRepository
import somerfield.howamiservice.repositories.UserAccountRepository
import somerfield.howamiservice.resources.ConfirmationNotificationsResource
import somerfield.howamiservice.resources.LoginResource
import somerfield.howamiservice.resources.RegistrationConfirmationResource
import somerfield.howamiservice.resources.UserRegistrationResource
import somerfield.howamiservice.wire.JSON
import somerfield.resources.RequestIdSource


class HowAmIServiceApplication : Application<HowamiServiceConfiguration>() {

    override fun run(configuration: HowamiServiceConfiguration, environment: Environment) {


        environment.healthChecks().register("basic", object : HealthCheck() {
            override fun check(): Result {
                return Result.healthy()
            }
        })

        val binding = HowamiServiceBinding.new(configuration)
        environment.jersey().register(binding.userRegistrationResource())
        environment.jersey().register(binding.registrationConfirmationResource())
        environment.jersey().register(binding.loginResource())
        environment.jersey().register(binding.confirmationNotificationResource())
        JSON.configureObjectMapper(environment.objectMapper)

    }

    override fun initialize(bootstrap: Bootstrap<HowamiServiceConfiguration>) {
        bootstrap.configurationSourceProvider = SubstitutingSourceProvider(
                bootstrap.configurationSourceProvider,
                EnvironmentVariableSubstitutor()
        )

        bootstrap.addBundle(object : SwaggerBundle<HowamiServiceConfiguration>() {
            override fun getSwaggerBundleConfiguration(configuration: HowamiServiceConfiguration): SwaggerBundleConfiguration {
                return SwaggerBundleConfiguration().apply {
                    this.resourcePackage = "somerfield.howamiservice.resources"
                }
            }
        })
    }
}

fun main(args: Array<String>) {
    val arguments = if (args.isEmpty()) arrayOf("server") else args
    HowAmIServiceApplication().run(*arguments)
}

data class HowamiServiceConfiguration
@JsonCreator
constructor(
        @JsonProperty("mongoHost")
        private val mongoHost: String?,
        @JsonProperty("mongoDatabase")
        private val mongoDatabase: String?,
        @JsonProperty("kafkaBootstrapServers")
        private val kafkaBootstrapServers: String?
) : Configuration() {

    constructor() : this(null, null, null)

    fun getMongoHost(): String {
        return mongoHost ?: "localhost"
    }

    fun getMongoDatabase(): String {
        return mongoHost ?: "howami"
    }

    fun getKafkaBootstrapServers(): String {
        return kafkaBootstrapServers ?: "localhost:9092"
    }
}

/**
 * Low tech injection
 */
class HowamiServiceBinding(private val configuration: HowamiServiceConfiguration) {

    private val requestIdSource = RequestIdSource()

    private val registrationConfirmationService = RegistrationConfirmationService(
            registrationConfirmationRepository = registrationConfirmationRepository(),
            userAccountRepository = userAccountRepository(),
            confirmationCodeGenerator = confirmationCodeGenerator()
    )

    private val randomStringGenerator = RandomStringGenerator.Builder().withinRange(arrayOf('A', 'Z').toCharArray()).build()

    private fun confirmationCodeGenerator() = { randomStringGenerator.generate(5) }

    private fun requestIdSource() = requestIdSource //TODO: This needs to be replaced with something that reads headers

    fun registrationConfirmationResource() = RegistrationConfirmationResource(registrationConfirmationService(), requestIdSource())

    private fun registrationConfirmationService(): RegistrationConfirmationService {
        return registrationConfirmationService
    }

    private fun registrationConfirmationRepository(): RegistrationConfirmationRepository {
        return RegistrationConfirmationRepository(mongoDatabase().getCollection("registration_confirmation"))
    }

    fun userRegistrationResource() = UserRegistrationResource(
            userRegistrationService = userRegistrationService(),
            requestIdSource = requestIdSource()
    )

    fun loginResource() = LoginResource(loginService())

    private fun loginService() = LoginService(
            userAccountRepository(),
            hashPasswordFn()
    )

    private fun userRegistrationService() = UserRegistrationService(
            userAccountRepository = userAccountRepository(),
            registrationConfirmationService = registrationConfirmationService(),
            hashPassword = hashPasswordFn(),
            userEventProducer = userEventProducer()
    )

    private fun userEventProducer(): UserEventProducer = UserEventProducer(kafkaProducer())

    private fun kafkaProducer() = KafkaProducer<Unit, ByteArray>(kafkaProperties())

    private fun kafkaProperties() = mapOf(
            "bootstrap.servers" to configuration.getKafkaBootstrapServers(),
            "key.serializer" to "org.apache.kafka.common.serialization.ByteArraySerializer",
            "value.serializer" to "org.apache.kafka.common.serialization.ByteArraySerializer"
    )

    private fun hashPasswordFn() = { password: String -> password } //TODO: implement scrypt-based hashing

    private fun mongoDatabase(): MongoDatabase = MongoClient(configuration.getMongoHost()).getDatabase(configuration.getMongoDatabase())

    private fun userAccountRepository() = UserAccountRepository(mongoDatabase().getCollection("user_account"))

    companion object {
        fun new(howamiServiceConfiguration: HowamiServiceConfiguration) = HowamiServiceBinding(howamiServiceConfiguration)
    }

    fun confirmationNotificationResource() = ConfirmationNotificationsResource(requestIdSource, registrationConfirmationService())
}