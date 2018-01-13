package somerfield.testing

import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.json.JSONObject
import java.net.URI

object HTTP {
    fun get(to: URI, headers: Map<String, String> = emptyMap()): HttpResponse {
        val requestConfig = RequestConfig.custom()
                .setConnectTimeout(1000)
                .setSocketTimeout(1000)
                .setConnectionRequestTimeout(5000).build()
        val get = HttpGet(to)
        get.setHeaders(headers.toList().map { BasicHeader(it.first, it.second) }.toTypedArray())
        return HttpResponse(HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build().execute(get))
    }

    fun post(to: URI, content: String, contentType: String, headers: Map<String, String> = emptyMap()): HttpResponse {
        val post = HttpPost(to)
        headers.forEach { header -> post.addHeader(BasicHeader(header.key, header.value))}
        val entity = StringEntity(content)
        entity.setContentType(contentType)
        post.entity = entity
        val responseImpl = HttpClientBuilder.create().build().execute(post)
        return HttpResponse(responseImpl)
    }

}

data class HttpResponse(private val responseImpl: CloseableHttpResponse) {
    val status: Int
        get() = responseImpl.statusLine.statusCode

    val json: JSONObject
        get() {
            return JSONObject(responseImpl.entity.content.bufferedReader().use {
                it.readText()
            })
        }

}