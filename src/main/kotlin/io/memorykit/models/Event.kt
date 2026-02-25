package io.memorykit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * A user event.
 */
@Serializable
data class Event(
    val id: String? = null,
    val type: String,
    val data: JsonObject? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

/**
 * Request body for creating an event.
 */
@Serializable
data class CreateEventRequest(
    val type: String,
    val data: JsonObject? = null
)
