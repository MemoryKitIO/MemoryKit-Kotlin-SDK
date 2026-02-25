package io.memorykit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Request body for RAG query.
 */
@Serializable
data class QueryRequest(
    val query: String,
    @SerialName("max_sources")
    val maxSources: Int? = null,
    val temperature: Double? = null,
    val mode: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val instructions: String? = null,
    @SerialName("response_format")
    val responseFormat: String? = null,
    @SerialName("include_graph")
    val includeGraph: Boolean? = null,
    val filters: Filters? = null,
    val stream: Boolean? = null
)

/**
 * Response from RAG query.
 */
@Serializable
data class QueryResponse(
    val answer: String,
    val confidence: Double? = null,
    val sources: List<Source>? = null,
    val model: String? = null,
    @SerialName("request_id")
    val requestId: String? = null,
    val usage: Usage? = null
)

