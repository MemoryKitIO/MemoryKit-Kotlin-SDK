package io.memorykit.resources

import io.memorykit.HttpClient
import io.memorykit.models.StatusResponse

/**
 * Resource for checking API status.
 */
class StatusResource internal constructor(private val http: HttpClient) {

    /**
     * Get the current API status.
     *
     * @return The [StatusResponse].
     */
    suspend fun get(): StatusResponse {
        val response = http.get("/status")
        return http.json.decodeFromString(StatusResponse.serializer(), response)
    }
}
