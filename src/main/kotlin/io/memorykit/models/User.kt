package io.memorykit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * A user in MemoryKit.
 */
@Serializable
data class User(
    val id: String,
    val email: String? = null,
    val name: String? = null,
    val metadata: JsonObject? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Request body for upserting a user.
 */
@Serializable
data class UpsertUserRequest(
    val id: String,
    val email: String? = null,
    val name: String? = null,
    val metadata: JsonObject? = null
)

/**
 * Request body for updating a user.
 */
@Serializable
data class UpdateUserRequest(
    val email: String? = null,
    val name: String? = null,
    val metadata: JsonObject? = null
)
