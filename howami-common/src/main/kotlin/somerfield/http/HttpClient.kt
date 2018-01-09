package somerfield.http

import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import java.io.Closeable
import java.io.InputStream
import java.net.URI
import java.util.*

class HttpClient {
    fun get(to: URI, headers: Map<String, String> = emptyMap()): HttpResponse {
        val requestConfig = RequestConfig.custom()
                .setConnectTimeout(1000)
                .setSocketTimeout(1000)
                .setConnectionRequestTimeout(5000).build()
        val get = HttpGet(to)
        get.setHeaders(headers.toList().map { BasicHeader(it.first, it.second) }.toTypedArray())
        client().execute(get).entity.content
        return HttpResponseImpl(HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build().execute(get))
    }

    private fun client(): CloseableHttpClient {
        return HttpClientBuilder.create().build()
    }
}

abstract class HttpResponse() : Closeable {
    abstract val httpResponse: Int
    abstract val entityStream: Optional<InputStream>
}

class HttpResponseImpl(private val responseImpl: CloseableHttpResponse) : HttpResponse() {
    override val entityStream: Optional<InputStream>
        get() = Optional.ofNullable(responseImpl.entity).map { it.content }

    override val httpResponse: Int
        get() = responseImpl.statusLine.statusCode

    override fun close() {
        responseImpl.close()
    }
}