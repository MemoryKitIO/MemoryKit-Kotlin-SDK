package io.memorykit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * A memory object stored in MemoryKit.
 */
@Serializable
data class Memory(
    val id: String,
    val title: String? = null,
    val content: String? = null,
    val type: String? = null,
    val status: String? = null,
    val tags: List<String>? = null,
    val metadata: JsonObject? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val language: String? = null,
    val format: String? = null,
    @SerialName("chunks_count")
    val chunksCount: Int? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Request body for creating a memory.
 */
@Serializable
data class CreateMemoryRequest(
    val content: String,
    val title: String? = null,
    val type: String? = null,
    val tags: List<String>? = null,
    val metadata: JsonObject? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val language: String? = null,
    val format: String? = null
)

/**
 * Request body for updating a memory.
 */
@Serializable
data class UpdateMemoryRequest(
    val title: String? = null,
    val type: String? = null,
    val tags: List<String>? = null,
    val metadata: JsonObject? = null,
    val content: String? = null
)

/**
 * Request body for batch memory ingestion.
 */
@Serializable
data class BatchIngestRequest(
    val items: List<CreateMemoryRequest>,
    val defaults: BatchDefaults? = null
)

/**
 * Default values applied to all items in a batch.
 */
@Serializable
data class BatchDefaults(
    val type: String? = null,
    val tags: List<String>? = null,
    val metadata: JsonObject? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val language: String? = null,
    val format: String? = null
)

/**
 * A single item result from batch ingestion.
 */
@Serializable
data class BatchMemoryResult(
    val id: String,
    val title: String? = null,
    val status: String? = null,
    val index: Int? = null
)

/**
 * Response from batch ingestion.
 */
@Serializable
data class BatchIngestResponse(
    val items: List<BatchMemoryResult> = emptyList(),
    val total: Int? = null,
    val failed: Int? = null,
    val errors: List<BatchError>? = null
)

@Serializable
data class BatchError(
    val index: Int? = null,
    val error: String? = null,
    val message: String? = null,
    val code: String? = null
)
