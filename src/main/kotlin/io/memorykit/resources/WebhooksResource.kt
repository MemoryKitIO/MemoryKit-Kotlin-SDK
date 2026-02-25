package io.memorykit.resources

import io.memorykit.HttpClient
import io.memorykit.models.*

/**
 * Resource for managing webhook registrations.
 */
class WebhooksResource internal constructor(private val http: HttpClient) {

    /**
     * Register a new webhook.
     *
     * @param url The webhook URL to receive events.
     * @param events List of event types to subscribe to.
     * @return The created [Webhook].
     */
    suspend fun create(
        url: String,
        events: List<String>? = null
    ): Webhook {
        val request = CreateWebhookRequest(url = url, events = events)
        val body = http.json.encodeToString(CreateWebhookRequest.serializer(), request)
        val response = http.post("/webhooks", body)
        return http.json.decodeFromString(Webhook.serializer(), response)
    }

    /**
     * List all registered webhooks.
     *
     * @return A list of [Webhook] objects.
     */
    suspend fun list(): List<Webhook> {
        val response = http.get("/webhooks")
        return http.json.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(Webhook.serializer()),
            response
        )
    }

    /**
     * Get a webhook by ID.
     *
     * @param id The webhook ID.
     * @return The [Webhook].
     */
    suspend fun get(id: String): Webhook {
        val response = http.get("/webhooks/$id")
        return http.json.decodeFromString(Webhook.serializer(), response)
    }

    /**
     * Delete a webhook.
     *
     * @param id The webhook ID.
     */
    suspend fun delete(id: String) {
        http.delete("/webhooks/$id")
    }

    /**
     * Test a webhook by sending a test event.
     *
     * @param id The webhook ID.
     * @return The [WebhookTestResponse] with test results.
     */
    suspend fun test(id: String): WebhookTestResponse {
        val response = http.post("/webhooks/$id/test")
        return http.json.decodeFromString(WebhookTestResponse.serializer(), response)
    }
}
