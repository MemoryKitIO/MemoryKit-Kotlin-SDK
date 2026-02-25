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

    /**
     * Perform a RAG query against stored memories.
     *
     * @param query The query string (required).
     * @param maxSources Maximum number of source documents to include.
     * @param temperature LLM temperature for response generation.
     * @param mode Query mode (e.g., "balanced", "precise", "creative").
     * @param userId Scope the query to a specific user's memories.
     * @param instructions Additional instructions for the LLM.
     * @param responseFormat Desired response format.
     * @param includeGraph Whether to include knowledge graph data.
     * @param filters Search filters.
     * @return The [QueryResponse] with answer, sources, and usage info.
     */
    suspend fun query(
        query: String,
        maxSources: Int? = null,
        temperature: Double? = null,
        mode: String? = null,
        userId: String? = null,
        instructions: String? = null,
        responseFormat: String? = null,
        includeGraph: Boolean? = null,
        filters: Filters? = null
    ): QueryResponse {
        val request = QueryRequest(
            query = query,
            maxSources = maxSources,
            temperature = temperature,
            mode = mode,
            userId = userId,
            instructions = instructions,
            responseFormat = responseFormat,
            includeGraph = includeGraph,
            filters = filters
        )
        val body = http.json.encodeToString(QueryRequest.serializer(), request)
        val response = http.post("/memories/query", body)
        return http.json.decodeFromString(QueryResponse.serializer(), response)
    }

    /**
     * Perform a hybrid search across memories.
     *
     * @param query Search query string.
     * @param limit Maximum number of results.
     * @param scoreThreshold Minimum relevance score threshold.
     * @param includeGraph Whether to include knowledge graph data.
     * @param filters Search filters.
     * @param userId Scope search to a specific user.
     * @return The [SearchResponse] with results and optional graph data.
     */
    suspend fun search(
        query: String,
        limit: Int? = null,
        scoreThreshold: Double? = null,
        includeGraph: Boolean? = null,
        filters: Filters? = null,
        userId: String? = null
    ): SearchResponse {
        val request = SearchRequest(
            query = query,
            limit = limit,
            scoreThreshold = scoreThreshold,
            includeGraph = includeGraph,
            filters = filters,
            userId = userId
        )
        val body = http.json.encodeToString(SearchRequest.serializer(), request)
        val response = http.post("/memories/search", body)
        return http.json.decodeFromString(SearchResponse.serializer(), response)
    }

    /**
     * Stream a RAG query response using Server-Sent Events.
     *
     * Returns a Flow of [SSEEvent] objects. Event types include:
     * - "text": Streaming text content
     * - "sources": Source documents
     * - "usage": Token usage information
     * - "done": Stream complete
     * - "error": Stream error
     *
     * @param query The query string (required).
     * @param maxSources Maximum number of source documents.
     * @param temperature LLM temperature.
     * @param mode Query mode.
     * @param userId Scope to user.
     * @param instructions Additional instructions.
     * @param responseFormat Response format.
     * @param includeGraph Include graph data.
     * @param filters Search filters.
     * @return A [Flow] of [SSEEvent] objects.
     */
    fun stream(
        query: String,
        maxSources: Int? = null,
        temperature: Double? = null,
        mode: String? = null,
        userId: String? = null,
        instructions: String? = null,
        responseFormat: String? = null,
        includeGraph: Boolean? = null,
        filters: Filters? = null
    ): Flow<SSEEvent> {
        val request = StreamRequest(
            query = query,
            maxSources = maxSources,
            temperature = temperature,
            mode = mode,
            userId = userId,
            instructions = instructions,
            responseFormat = responseFormat,
            includeGraph = includeGraph,
            filters = filters
        )
        val body = http.json.encodeToString(StreamRequest.serializer(), request)

        return kotlinx.coroutines.flow.flow {
            val response = http.postForStream("/memories/stream", body)
            val events = SSEParser.parse(response)
            events.collect { emit(it) }
        }
    }
}
