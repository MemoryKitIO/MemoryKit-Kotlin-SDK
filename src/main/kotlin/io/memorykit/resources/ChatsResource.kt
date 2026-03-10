package io.memorykit.resources

// V2: Chats resource disabled for initial launch.
// The entire chats API (create, list, get, sendMessage, streamMessage, delete)
// will be re-enabled when the LLM-powered chat endpoints are available.

// import io.memorykit.HttpClient
// import io.memorykit.models.*
// import io.memorykit.sse.SSEParser
// import kotlinx.coroutines.flow.Flow
// import kotlinx.serialization.json.JsonObject
//
// class ChatsResource internal constructor(private val http: HttpClient) {
//
//     suspend fun create(
//         userId: String? = null,
//         title: String? = null,
//         metadata: JsonObject? = null
//     ): Chat {
//         val request = CreateChatRequest(userId = userId, title = title, metadata = metadata)
//         val body = http.json.encodeToString(CreateChatRequest.serializer(), request)
//         val response = http.post("/chats", body)
//         return http.json.decodeFromString(Chat.serializer(), response)
//     }
//
//     suspend fun list(
//         userId: String? = null,
//         limit: Int? = null,
//         cursor: String? = null
//     ): ListResponse<Chat> {
//         val params = mapOf("user_id" to userId, "limit" to limit?.toString(), "cursor" to cursor)
//         val response = http.get("/chats", params)
//         return http.json.decodeFromString(ListResponse.serializer(Chat.serializer()), response)
//     }
//
//     suspend fun get(id: String): Chat {
//         val response = http.get("/chats/$id/messages")
//         return http.json.decodeFromString(Chat.serializer(), response)
//     }
//
//     suspend fun sendMessage(chatId: String, message: String, mode: String? = null): SendMessageResponse {
//         val request = SendMessageRequest(message = message, mode = mode)
//         val body = http.json.encodeToString(SendMessageRequest.serializer(), request)
//         val response = http.post("/chats/$chatId/messages", body)
//         return http.json.decodeFromString(SendMessageResponse.serializer(), response)
//     }
//
//     fun streamMessage(chatId: String, message: String, mode: String? = null): Flow<SSEEvent> {
//         val request = SendMessageRequest(message = message, mode = mode)
//         val body = http.json.encodeToString(SendMessageRequest.serializer(), request)
//         return kotlinx.coroutines.flow.flow {
//             val response = http.postForStream("/chats/$chatId/messages/stream", body)
//             val events = SSEParser.parse(response)
//             events.collect { emit(it) }
//         }
//     }
//
//     suspend fun delete(id: String) {
//         http.delete("/chats/$id")
//     }
// }
