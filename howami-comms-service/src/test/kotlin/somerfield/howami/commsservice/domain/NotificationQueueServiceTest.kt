package somerfield.howami.commsservice.domain

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.CoreMatchers.`is`
import org.json.JSONObject
import org.junit.Assert.assertThat
import org.junit.Test

import org.junit.Ignore
import somerfield.http.HttpClient
import somerfield.http.HttpResponse
import java.io.InputStream
import java.net.URI
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

class NotificationQueueServiceTest {

    private val pendingNotificationsURI = URI.create("http://example.com/api/v1/confirmation-notifications")
    private val now = Instant.now()

    private val notificationJSON = JSONObject("""
        {
            "header":{
                "request-id": ${UUID.randomUUID()}
            },
            "body":[{
                "email":"email1@example.com",
                "user-id":"abcde",
                "confirmation-code": "ABCDE",
                "created-datetime": "${DateTimeFormatter.ISO_INSTANT.format(now)}",
                "confirmation-status": "QUEUED"
            },
            {
                "email":"email2@example.com",
                "user-id":"bcdef",
                "confirmation-code": "BCDEF",
                "created-datetime": "${DateTimeFormatter.ISO_INSTANT.format(now)}",
                "confirmation-status": "QUEUED"
            }]
        }
    """.trimIndent())
            .toString()

    private val response = mock<HttpResponse> {
        on { httpResponse } doReturn (200)
        on { entityStream } doReturn (Optional.of<InputStream>(notificationJSON.byteInputStream()))
    }

    private val response404 = mock<HttpResponse> {
        on { httpResponse } doReturn (404)
        on { entityStream } doReturn (Optional.empty())
    }

    private val httpClient = mock<HttpClient> {
        on { get(any(), any()) } doReturn (response404)
        on { get(pendingNotificationsURI) } doReturn (response)
    }

    @Test
    fun getPendingNotificationsWhenSuccessfulResponse() {
        val notifications = NotificationQueueService(
                httpClient = httpClient,
                baseURI = "http://example.com"
        ).getPendingNotifications()
        assertThat(notifications, `is`(listOf(
                PendingNotification("abcde", "email1@example.com", "ABCDE"),
                PendingNotification("bcdef", "email2@example.com", "BCDEF")

        )))
    }

    //TODO: testing non-200 results

    @Test
    @Ignore
    fun confirmingNotificationSentWithExistingNotification() {

    }
}