package io.memorykit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Search precision level controlling the relevance threshold.
 */
enum class SearchPrecision(val value: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    override fun toString(): String = value
}

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
