package io.memorykit.models

// V2: Chat types disabled for initial launch.
// The chats API will be re-enabled when LLM-powered chat endpoints are available.

// import kotlinx.serialization.SerialName
// import kotlinx.serialization.Serializable
// import kotlinx.serialization.json.JsonObject
//
// @Serializable
// data class Chat(
//     val id: String,
//     @SerialName("user_id")
//     val userId: String? = null,
//     val title: String? = null,
//     val metadata: JsonObject? = null,
//     val messages: List<ChatMessage>? = null,
//     @SerialName("created_at")
//     val createdAt: String? = null,
//     @SerialName("updated_at")
//     val updatedAt: String? = null
// )
//
// @Serializable
// data class ChatMessage(
//     val id: String? = null,
//     val role: String? = null,
//     val content: String? = null,
//     @SerialName("created_at")
//     val createdAt: String? = null
// )
//
// @Serializable
// data class CreateChatRequest(
//     @SerialName("user_id")
//     val userId: String? = null,
//     val title: String? = null,
//     val metadata: JsonObject? = null
// )
//
// @Serializable
// data class SendMessageRequest(
//     val message: String,
//     val mode: String? = null
// )
//
// @Serializable
// data class SendMessageResponse(
//     val message: ChatMessage,
//     val sources: List<Source>? = null,
//     val usage: Usage? = null,
//     @SerialName("request_id")
//     val requestId: String? = null
// )
