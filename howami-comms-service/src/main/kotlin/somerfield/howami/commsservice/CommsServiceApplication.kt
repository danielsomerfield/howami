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
import somerfield.howami.commsservice.domain.NotificationQueueService
import somerfield.howami.commsservice.domain.UserNotificationService
import somerfield.howami.commsservice.jobs.*
import somerfield.howamiservice.wire.JSON
import somerfield.time.TimeInterval
import somerfield.time.minute
import somerfield.time.seconds

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

    private val defaultNotificationInterval = 1.minute().inSeconds()
    private val defaultHowamiServiceHost: String = "localhost:8080"

    fun getTestMode(): Boolean {
        return testMode ?: true
    }

    fun getNotificationIntervalSeconds(): Long {
        return notificationIntervalSeconds ?: defaultNotificationInterval
    }

    fun getHowamiServiceHost(): String {
        return howamiServiceHost ?: defaultHowamiServiceHost;
    }
}

class CommsServiceBinding(private val commsServiceConfiguration: CommsServiceConfiguration) {

    private val jobScheduler = JobScheduler()

    fun bind() {
        jobs().forEach { job -> jobScheduler.schedule(job.job, job.interval) }
    }

    private fun jobs(): List<ScheduledJob> = listOf(
            ScheduledJob(
                    SendNotificationsJob(
                            notificationQueueService = notificationQueueService(),
                            userNotificationService = userNotificationService(),
                            notificationConfiguration = notificationConfiguration()
                    )::runJob,
                    sendNotificationsInterval()
            )
    )

    private fun sendNotificationsInterval() = commsServiceConfiguration.getNotificationIntervalSeconds().seconds()

    private fun notificationConfiguration() = NotificationConfiguration(
            disableSending = commsServiceConfiguration.getTestMode()
    )

    private fun notificationQueueService() = NotificationQueueService()

    private fun userNotificationService() = UserNotificationService()


}

data class ScheduledJob(
        val job: Job,
        val interval: TimeInterval
)