package somerfield.howami.commsservice

import com.codahale.metrics.health.HealthCheck
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.configuration.EnvironmentVariableSubstitutor
import io.dropwizard.configuration.SubstitutingSourceProvider
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import somerfield.howami.commsservice.domain.MessageBuilder
import somerfield.howami.commsservice.domain.NotificationEventProducer
import somerfield.howami.commsservice.domain.NotificationQueueService
import somerfield.howami.commsservice.domain.UserNotificationService
import somerfield.howamiservice.wire.JSON

class CommsServiceApplication : Application<CommsServiceConfiguration>() {

    override fun run(configuration: CommsServiceConfiguration, environment: Environment) {

        environment.healthChecks().register("basic", object : HealthCheck() {
            override fun check(): Result {
                return Result.healthy()
            }
        })

        CommsServiceBinding(configuration).bind()

        JSON.configureObjectMapper(environment.objectMapper)
    }

    override fun initialize(bootstrap: Bootstrap<CommsServiceConfiguration>) {
        bootstrap.configurationSourceProvider = SubstitutingSourceProvider(
                bootstrap.configurationSourceProvider,
                EnvironmentVariableSubstitutor()
        )
    }
}

fun main(args: Array<String>) {
    val arguments = if (args.isEmpty()) arrayOf("server") else args
    CommsServiceApplication().run(*arguments)
}

class CommsServiceConfiguration
@JsonCreator
constructor(
        @JsonProperty("testMode")
        private val testMode: Boolean?,
        @JsonProperty("notificationIntervalSeconds")
        private val notificationIntervalSeconds: Long?,
        @JsonProperty("howamiServiceHost")
        private val howamiServiceHost: String?
) : Configuration() {

    constructor() : this(testMode = null, notificationIntervalSeconds = null, howamiServiceHost = null)

    fun getTestMode(): Boolean {
        return testMode ?: true
    }
}

class CommsServiceBinding(private val commsServiceConfiguration: CommsServiceConfiguration) {

    fun bind() {
//        NotificationQueueService(
//                userNotificationService = userNotificationService(),
//                userNotificationEventProducer = NotificationEventProducer(kafkaProducer()),
//                messageBuilder = messageBuilder(),
//                testMode = { commsServiceConfiguration.getTestMode() }
//        )
    }

    private fun kafkaProducer(): Producer<Unit, ByteArray> {
        TODO()
    }

    private fun messageBuilder(): MessageBuilder = { _, _, _ -> "NYI" }

    private fun userNotificationService() = UserNotificationService()

}
