package io.memorykit.resources

import io.memorykit.HttpClient
import io.memorykit.models.*
import io.memorykit.sse.SSEParser
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject

/**
 * Resource for managing chat sessions and messages.
 */
class ChatsResource internal constructor(private val http: HttpClient) {

    /**
     * Create a new chat session.
     *
     * @param userId Optional user ID to associate the chat with.
     * @param title Optional chat title.
     * @param metadata Optional metadata.
     * @return The created [Chat].
     */
    suspend fun create(
        userId: String? = null,
        title: String? = null,
        metadata: JsonObject? = null
    ): Chat {
        val request = CreateChatRequest(
            userId = userId,
            title = title,
            metadata = metadata
        )
        val body = http.json.encodeToString(CreateChatRequest.serializer(), request)
        val response = http.post("/chats", body)
        return http.json.decodeFromString(Chat.serializer(), response)
    }

    /**
     * List chat sessions with cursor-based pagination.
     *
     * @param userId Filter by user ID.
     * @param limit Maximum number of results (default 20).
     * @param cursor Pagination cursor.
     * @return A [ListResponse] containing chats.
     */
    suspend fun list(
        userId: String? = null,
        limit: Int? = null,
        cursor: String? = null
    ): ListResponse<Chat> {
        val params = mapOf(
            "user_id" to userId,
            "limit" to limit?.toString(),
            "cursor" to cursor
        )
        val response = http.get("/chats", params)
        return http.json.decodeFromString(ListResponse.serializer(Chat.serializer()), response)
    }

    /**
     * Get a chat session with its message history.
     *
     * @param id The chat ID.
     * @return The [Chat] with messages.
     */
    suspend fun get(id: String): Chat {
        val response = http.get("/chats/$id")
        return http.json.decodeFromString(Chat.serializer(), response)
    }

    /**
     * Send a message in a chat session.
     *
     * @param chatId The chat ID.
     * @param message The message content (required).
     * @param mode Optional query mode.
     * @return The [SendMessageResponse] with the assistant's reply.
     */
    suspend fun sendMessage(
        chatId: String,
        message: String,
        mode: String? = null
    ): SendMessageResponse {
        val request = SendMessageRequest(message = message, mode = mode)
        val body = http.json.encodeToString(SendMessageRequest.serializer(), request)
        val response = http.post("/chats/$chatId/messages", body)
        return http.json.decodeFromString(SendMessageResponse.serializer(), response)
    }

    /**
     * Stream a message response in a chat session using SSE.
     *
     * @param chatId The chat ID.
     * @param message The message content (required).
     * @param mode Optional query mode.
     * @return A [Flow] of [SSEEvent] objects.
     */
    fun streamMessage(
        chatId: String,
        message: String,
        mode: String? = null
    ): Flow<SSEEvent> {
        val request = SendMessageRequest(message = message, mode = mode)
        val body = http.json.encodeToString(SendMessageRequest.serializer(), request)

        return kotlinx.coroutines.flow.flow {
            val response = http.postForStream("/chats/$chatId/messages/stream", body)
            val events = SSEParser.parse(response)
            events.collect { emit(it) }
        }
    }

    /**
     * Delete a chat session.
     *
     * @param id The chat ID.
     */
    suspend fun delete(id: String) {
        http.delete("/chats/$id")
    }
}
