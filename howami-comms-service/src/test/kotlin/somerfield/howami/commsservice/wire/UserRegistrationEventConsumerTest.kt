package somerfield.howami.commsservice.wire

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import somerfield.howami.commsservice.domain.UserRegistrationEvent
import java.util.*

class UserRegistrationEventConsumerTest {

    private val userId = UUID.randomUUID().toString()
    private val emailAddress = "${UUID.randomUUID()}@example.com"
    private val confirmationCode = UUID.randomUUID().toString()
    private val topicName = "user-registration-event"

    private val event1Bytes = """
    {
        "body": {
            "user-id": "$userId",
            "email": "$emailAddress",
            "confirmation-code": "$confirmationCode"
        }
    }
    """.trimIndent().toByteArray(Charsets.UTF_8)

    private val kafkaConsumer = mock<Consumer<Unit, ByteArray>>() {
        on {
            poll(any())
        } doReturn (ConsumerRecords(mutableMapOf(
                TopicPartition(topicName, 0) to mutableListOf(
                        ConsumerRecord(
                                topicName, 0, 0, Unit, event1Bytes
                        ))
        )))
    }

    private val collected = mutableListOf<UserRegistrationEvent>()

    private val userRegistrationEventConsumer = UserRegistrationEventConsumer(
            kafkaConsumer = kafkaConsumer,
            callback = { evt -> collected.add(evt) }
    )

    @Before
    fun setup() {
        collected.clear()
    }

    @Test
    fun pollOnceWithData() {
        userRegistrationEventConsumer.pollOnce()
        assertThat(collected, `is`(listOf(UserRegistrationEvent(
                userId = userId,
                emailAddress = emailAddress,
                confirmationCode = confirmationCode
        )).asIterable()))
    }

    @Test
    fun filterOutBadData() {
        val withBadRecords = mutableMapOf(
                TopicPartition(topicName, 0) to mutableListOf(
                        ConsumerRecord<Unit, ByteArray>(
                                topicName, 0, 0, Unit, "{}".toByteArray()
                        ),
                        ConsumerRecord<Unit, ByteArray>(
                                topicName, 0, 0, Unit, event1Bytes
                        )
                )
        )
        whenever(kafkaConsumer.poll(any())).thenReturn(ConsumerRecords(withBadRecords))

        val result = userRegistrationEventConsumer.pollOnce()
        assertThat(collected, `is`(listOf(UserRegistrationEvent(
                userId = userId,
                emailAddress = emailAddress,
                confirmationCode = confirmationCode
        )).asIterable()))
    }

}