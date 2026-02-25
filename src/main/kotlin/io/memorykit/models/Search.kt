package io.memorykit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Request body for hybrid search.
 */
@Serializable
data class SearchRequest(
    val query: String,
    val limit: Int? = null,
    @SerialName("score_threshold")
    val scoreThreshold: Double? = null,
    @SerialName("include_graph")
    val includeGraph: Boolean? = null,
    val filters: Filters? = null,
    @SerialName("user_id")
    val userId: String? = null
)

/**
 * Response from hybrid search.
 */
@Serializable
data class SearchResponse(
    val results: List<SearchResult>,
    val graph: GraphData? = null,
    @SerialName("request_id")
    val requestId: String? = null,
    @SerialName("total_results")
    val totalResults: Int? = null
)

/**
 * Individual search result.
 */
@Serializable
data class SearchResult(
    val id: String,
    val title: String? = null,
    val content: String? = null,
    val score: Double? = null,
    val type: String? = null,
    val tags: List<String>? = null,
    val metadata: JsonObject? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
