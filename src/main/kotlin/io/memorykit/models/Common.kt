package io.memorykit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Cursor-based paginated list response.
 */
@Serializable
data class ListResponse<T>(
    val data: List<T>,
    @SerialName("has_more")
    val hasMore: Boolean = false,
    val cursor: String? = null
)

/**
 * Server-Sent Event from a streaming endpoint.
 */
data class SSEEvent(
    val event: String,
    val data: String,
    val id: String? = null
)

/**
 * Usage information returned from query/stream endpoints.
 */
@Serializable
data class Usage(
    @SerialName("prompt_tokens")
    val promptTokens: Int? = null,
    @SerialName("completion_tokens")
    val completionTokens: Int? = null,
    @SerialName("total_tokens")
    val totalTokens: Int? = null
)

/**
 * Source reference from a query response.
 */
@Serializable
data class Source(
    val id: String? = null,
    val title: String? = null,
    val content: String? = null,
    val score: Double? = null,
    val metadata: JsonObject? = null
)

/**
 * Graph data from search/query responses.
 */
@Serializable
data class GraphData(
    val nodes: List<GraphNode> = emptyList(),
    val edges: List<GraphEdge> = emptyList()
)

@Serializable
data class GraphNode(
    val id: String,
    val label: String? = null,
    val type: String? = null,
    val metadata: JsonObject? = null
)

@Serializable
data class GraphEdge(
    val source: String,
    val target: String,
    val label: String? = null,
    val weight: Double? = null
)

/**
 * Filters for search/query operations.
 */
@Serializable
data class Filters(
    val type: String? = null,
    val tags: List<String>? = null,
    val metadata: JsonObject? = null,
    @SerialName("created_after")
    val createdAfter: String? = null,
    @SerialName("created_before")
    val createdBefore: String? = null
)
