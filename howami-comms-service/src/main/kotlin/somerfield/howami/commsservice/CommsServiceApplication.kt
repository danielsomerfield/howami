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
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import somerfield.howami.commsservice.domain.*
import somerfield.howami.commsservice.wire.UserRegistrationEventConsumer
import somerfield.howamiservice.wire.JSON

class CommsServiceApplication : Application<CommsServiceConfiguration>() {

    override fun run(configuration: CommsServiceConfiguration, environment: Environment) {

        environment.healthChecks().register("basic", object : HealthCheck() {
            override fun check(): Result {
                return Result.healthy()
            }
        })

        CommsServiceBinding(configuration, environment).bind()

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
        @JsonProperty("kafkaBootstrapServers")
        private val kafkaBootstrapServers: String?
) : Configuration() {

    constructor() : this(
            testMode = null,
            kafkaBootstrapServers = null
    )

    fun getTestMode(): Boolean {
        return testMode ?: true
    }

    fun getKafkaBootstrapServers(): String {
        return kafkaBootstrapServers ?: "localhost:9092"
    }
}

class CommsServiceBinding(
        private val configuration: CommsServiceConfiguration,
        private val environment: Environment

) {

    fun bind() {
        val userRegistrationEventConsumer = userRegistrationEventConsumer()
        val eventConsumerExecutor = eventConsumerExecutor()
        eventConsumerExecutor.execute(userRegistrationEventConsumer)
    }

    private fun eventConsumerExecutor() = environment.lifecycle().executorService("event-consumer-executor").build()

    private fun userRegistrationEventConsumer(): UserRegistrationEventConsumer {
        val notificationQueueService = notificationQueueService()
        return UserRegistrationEventConsumer(
                kafkaConsumer = kafkaConsumer(),
                callback = { evt ->
                    notificationQueueService.userRegistered(evt)
                }
        )
    }

    private fun kafkaConsumer(): Consumer<Unit, ByteArray> {
        return KafkaConsumer<Unit, ByteArray>(kafkaConsumerProperties())
    }

    private fun notificationQueueService() = NotificationQueueService(
            userNotificationService = userNotificationService(),
            userNotificationEventProducer = NotificationEventProducer(kafkaProducer()),
            messageBuilder = messageBuilder(),
            testMode = { configuration.getTestMode() }
    )


    private fun kafkaProducer(): Producer<Unit, ByteArray> {
        return KafkaProducer<Unit, ByteArray>(kafkaProducerProperties())
    }

    private fun kafkaProducerProperties() = mapOf(
            "bootstrap.servers" to configuration.getKafkaBootstrapServers(),
            "key.serializer" to "org.apache.kafka.common.serialization.ByteArraySerializer",
            "value.serializer" to "org.apache.kafka.common.serialization.ByteArraySerializer")

    private fun kafkaConsumerProperties() = mapOf(
            "bootstrap.servers" to configuration.getKafkaBootstrapServers(),
            "group.id" to "comms-service-application",
            "enable.auto.commit" to "true",
            "key.deserializer" to "org.apache.kafka.common.serialization.ByteArrayDeserializer",
            "value.deserializer" to "org.apache.kafka.common.serialization.ByteArrayDeserializer"
    )

    private fun messageBuilder() = DefaultMessageBuilder.buildMessage

    private fun userNotificationService() = UserNotificationService()

}
