package io.memorykit.resources

import io.memorykit.HttpClient
import io.memorykit.models.*
import io.memorykit.sse.SSEParser
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.*

/**
 * Resource for managing memories.
 *
 * Provides CRUD operations, batch ingestion, RAG queries, hybrid search,
 * and SSE streaming.
 */
class MemoriesResource internal constructor(private val http: HttpClient) {

    /**
     * Create a new memory.
     *
     * @param content The memory content (required).
     * @param title Optional title.
     * @param type Optional type classification.
     * @param tags Optional list of tags.
     * @param metadata Optional metadata as a JsonObject.
     * @param userId Optional user ID to associate the memory with.
     * @param language Optional language code.
     * @param format Optional content format.
     * @return The created [Memory].
     */
    suspend fun create(
        content: String,
        title: String? = null,
        type: String? = null,
        tags: List<String>? = null,
        metadata: JsonObject? = null,
        userId: String? = null,
        language: String? = null,
        format: String? = null
    ): Memory {
        val request = CreateMemoryRequest(
            content = content,
            title = title,
            type = type,
            tags = tags,
            metadata = metadata,
            userId = userId,
            language = language,
            format = format
        )
        val body = http.json.encodeToString(CreateMemoryRequest.serializer(), request)
        val response = http.post("/memories", body)
        return http.json.decodeFromString(Memory.serializer(), response)
    }

    /**
     * Batch ingest multiple memories (max 100).
     *
     * @param items List of memory creation requests.
     * @param defaults Optional default values applied to all items.
     * @return The [BatchIngestResponse] with accepted/rejected counts.
     */
    suspend fun batchIngest(
        items: List<CreateMemoryRequest>,
        defaults: BatchDefaults? = null
    ): BatchIngestResponse {
        val request = BatchIngestRequest(items = items, defaults = defaults)
        val body = http.json.encodeToString(BatchIngestRequest.serializer(), request)
        val response = http.post("/memories/batch", body)
        return http.json.decodeFromString(BatchIngestResponse.serializer(), response)
    }

    /**
     * List memories with cursor-based pagination.
     *
     * @param limit Maximum number of results (default 20).
     * @param cursor Pagination cursor from a previous response.
     * @param status Filter by status.
     * @param type Filter by type.
     * @param userId Filter by user ID.
     * @return A [ListResponse] containing memories and pagination info.
     */
    suspend fun list(
        limit: Int? = null,
        cursor: String? = null,
        status: String? = null,
        type: String? = null,
        userId: String? = null
    ): ListResponse<Memory> {
        val params = mapOf(
            "limit" to limit?.toString(),
            "cursor" to cursor,
            "status" to status,
            "type" to type,
            "user_id" to userId
        )
        val response = http.get("/memories", params)
        return http.json.decodeFromString(ListResponse.serializer(Memory.serializer()), response)
    }

    /**
     * Get a single memory by ID.
     *
     * @param id The memory ID.
     * @return The [Memory].
     */
    suspend fun get(id: String): Memory {
        val response = http.get("/memories/$id")
        return http.json.decodeFromString(Memory.serializer(), response)
    }

    /**
     * Update a memory.
     *
     * @param id The memory ID.
     * @param title New title.
     * @param type New type.
     * @param tags New tags.
     * @param metadata New metadata.
     * @param content New content.
     * @return The updated [Memory].
     */
    suspend fun update(
        id: String,
        title: String? = null,
        type: String? = null,
        tags: List<String>? = null,
        metadata: JsonObject? = null,
        content: String? = null
    ): Memory {
        val request = UpdateMemoryRequest(
            title = title,
            type = type,
            tags = tags,
            metadata = metadata,
            content = content
        )
        val body = http.json.encodeToString(UpdateMemoryRequest.serializer(), request)
        val response = http.put("/memories/$id", body)
        return http.json.decodeFromString(Memory.serializer(), response)
    }

    /**
     * Delete a memory.
     *
     * @param id The memory ID.
     */
    suspend fun delete(id: String) {
        http.delete("/memories/$id")
    }

    // V2: query endpoint disabled for initial launch
    // suspend fun query(
    //     query: String,
    //     maxSources: Int? = null,
    //     temperature: Double? = null,
    //     mode: String? = null,
    //     userId: String? = null,
    //     instructions: String? = null,
    //     responseFormat: String? = null,
    //     includeGraph: Boolean? = null,
    //     filters: Filters? = null
    // ): QueryResponse {
    //     val request = QueryRequest(
    //         query = query,
    //         maxSources = maxSources,
    //         temperature = temperature,
    //         mode = mode,
    //         userId = userId,
    //         instructions = instructions,
    //         responseFormat = responseFormat,
    //         includeGraph = includeGraph,
    //         filters = filters
    //     )
    //     val body = http.json.encodeToString(QueryRequest.serializer(), request)
    //     val response = http.post("/memories/query", body)
    //     return http.json.decodeFromString(QueryResponse.serializer(), response)
    // }

    /**
     * Perform a hybrid search across memories.
     *
     * @param query Search query string (required).
     * @param precision Search precision level: LOW, MEDIUM, or HIGH (default: MEDIUM).
     * @param limit Maximum number of results (1–100, default: 10).
     * @param userId Scope search to a specific user.
     * @param type Filter by memory type.
     * @param tags Filter by tags (comma-separated).
     * @param createdAfter Filter memories created after this ISO 8601 timestamp.
     * @param createdBefore Filter memories created before this ISO 8601 timestamp.
     * @param includeGraph Whether to include knowledge graph data.
     * @return The [SearchResponse] with results and optional graph data.
     */
    suspend fun search(
        query: String,
        precision: SearchPrecision? = null,
        limit: Int? = null,
        userId: String? = null,
        type: String? = null,
        tags: String? = null,
        createdAfter: String? = null,
        createdBefore: String? = null,
        includeGraph: Boolean? = null
    ): SearchResponse {
        val params = mapOf(
            "query" to query,
            "precision" to precision?.value,
            "limit" to limit?.toString(),
            "user_id" to userId,
            "type" to type,
            "tags" to tags,
            "created_after" to createdAfter,
            "created_before" to createdBefore,
            "include_graph" to includeGraph?.toString()
        )
        val response = http.get("/memories/search", params)
        return http.json.decodeFromString(SearchResponse.serializer(), response)
    }

    // V2: streaming disabled for initial launch
    // fun stream(
    //     query: String,
    //     maxSources: Int? = null,
    //     temperature: Double? = null,
    //     mode: String? = null,
    //     userId: String? = null,
    //     instructions: String? = null,
    //     responseFormat: String? = null,
    //     includeGraph: Boolean? = null,
    //     filters: Filters? = null
    // ): Flow<SSEEvent> {
    //     val request = QueryRequest(
    //         query = query,
    //         maxSources = maxSources,
    //         temperature = temperature,
    //         mode = mode,
    //         userId = userId,
    //         instructions = instructions,
    //         responseFormat = responseFormat,
    //         includeGraph = includeGraph,
    //         filters = filters,
    //         stream = true
    //     )
    //     val body = http.json.encodeToString(QueryRequest.serializer(), request)
    //     return kotlinx.coroutines.flow.flow {
    //         val response = http.postForStream("/memories/query", body)
    //         val events = SSEParser.parse(response)
    //         events.collect { emit(it) }
    //     }
    // }
}
