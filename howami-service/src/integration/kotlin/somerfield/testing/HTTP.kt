package somerfield.testing

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.json.JSONObject
import java.net.URI

object HTTP {
    fun get(to: URI): HttpResponse {
        return HttpResponse(HttpClientBuilder.create().build().execute(HttpGet(to)))
    }

    fun post(to: URI, content: String, contentType: String): HttpResponse {
        val post = HttpPost(to)
        val entity = StringEntity(content)
        entity.setContentType(contentType)
        post.entity = entity
        return HttpResponse(HttpClientBuilder.create().build().execute(post))
    }

}

data class HttpResponse(val responseImpl: CloseableHttpResponse) {
    val status: Int
        get() = responseImpl.statusLine.statusCode

    val json: JSONObject
        get() {//TODO: make this reasonable twice
            return JSONObject(responseImpl.entity.content.bufferedReader().use { it.readText() })
        }

}