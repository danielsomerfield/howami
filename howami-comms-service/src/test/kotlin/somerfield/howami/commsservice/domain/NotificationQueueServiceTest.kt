package somerfield.howami.commsservice.domain

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.Test

import org.junit.Assert.*
import org.junit.Ignore
import somerfield.http.HttpClient
import somerfield.http.HttpResponse
import java.net.URI

class NotificationQueueServiceTest {

    val pendingNotificationsURI = URI.create("http://example.com/pending")

    val httpClient = mock<HttpClient> {
        on {
            get(pendingNotificationsURI)
        } doReturn (HttpResponse())
    }

    @Test
    @Ignore
    fun getPendingNotificationsWhenSuccessfulResponse() {
        val notifications = NotificationQueueService(
                httpClient = httpClient
        ).getPendingNotifications()
    }

    @Test
    @Ignore
    fun confirmingNotificationSentWithExistingNotification() {

    }
}

