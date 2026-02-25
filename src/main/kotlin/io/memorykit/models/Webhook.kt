package io.memorykit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A webhook registration.
 */
@Serializable
data class Webhook(
    val id: String,
    val url: String,
    val events: List<String>? = null,
    val active: Boolean? = null,
    val secret: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Request body for registering a webhook.
 */
@Serializable
data class CreateWebhookRequest(
    val url: String,
    val events: List<String>? = null
)

/**
 * Response from testing a webhook.
 */
@Serializable
data class WebhookTestResponse(
    val success: Boolean? = null,
    @SerialName("status_code")
    val statusCode: Int? = null,
    @SerialName("response_time_ms")
    val responseTimeMs: Long? = null,
    val message: String? = null
)
