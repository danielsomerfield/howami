package somerfield.howamiservice

import java.lang.System.getenv

object Env {

    val commsServiceHost = getenv().getOrDefault("HOWAMI_COMMS_SERVICE_SERVICE_HOST", "localhost")!!
    val commsServiceHealthPort = getenv().getOrDefault("HOWAMI_COMMS_SERVICE_SERVICE_PORT_HEALTH", "8081").toInt()

    val kafkaBootstrapServers = getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9094")!!

    val howamiServiceHost = getenv().getOrDefault("HOWAMI_SERVICE_SERVICE_HOST", "localhost")!!
    val howamiServiceHealthPort = getenv().getOrDefault("HOWAMI_SERVICE_SERVICE_PORT_HEALTH", "8081").toInt()
    val howamiServiceAppPort = getenv().getOrDefault("HOWAMI_SERVICE_SERVICE_PORT_APP", "8080").toInt()
}