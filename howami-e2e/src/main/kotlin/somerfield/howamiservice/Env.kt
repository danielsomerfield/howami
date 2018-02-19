package somerfield.howamiservice

import java.lang.System.getenv

object Env {

    val commsServiceHost = getenv().getOrDefault("HOWAMI_COMMS_SERVICE_SERVICE_HOST", "localhost")!!
    val commsServiceHealthPort = getenv().getOrDefault("HOWAMI_COMMS_SERVICE_SERVICE_PORT_HEALTH", "8081").toInt()

    private val kafkaHost = getenv().getOrDefault("KAFKA_SERVICE_SERVICE_HOST", "localhost")!!
    private val kafkaPort = getenv().getOrDefault("KAFKA_SERVICE_SERVICE_PORT", "9092").toInt()
    val kafkaBootstrapServers = "$kafkaHost:$kafkaPort"

    val howamiServiceHost = getenv().getOrDefault("HOWAMI_SERVICE_SERVICE_HOST", "localhost")!!
    val howamiServiceHealthPort = getenv().getOrDefault("HOWAMI_SERVICE_SERVICE_PORT_HEALTH", "8081").toInt()
    val howamiServiceAppPort = getenv().getOrDefault("HOWAMI_SERVICE_SERVICE_PORT_APP", "8080").toInt()
}