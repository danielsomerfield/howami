package somerfield.howamiservice.resources

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.annotations.Api
import java.util.*
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/api/v1/version")
@Api()
class VersionResource(private val objectMapper: ObjectMapper) {
    private val version: VersionWireType = getVersion()

    private fun getVersion(): VersionWireType {
        return Optional.ofNullable(javaClass.getResourceAsStream("/version.json")).map {
            objectMapper.readValue(it, VersionWireType::class.java)
        }.orElseGet {
            VersionWireType("Missing", "Missing")
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun version(): VersionWireType = version
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class VersionWireType
constructor(
        @JsonProperty("build-version")
        val buildVersion: String,

        @JsonProperty("build-time")
        val buildTime: String
)