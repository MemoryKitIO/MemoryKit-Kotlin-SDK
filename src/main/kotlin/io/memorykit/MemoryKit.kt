package io.memorykit

import io.memorykit.resources.*
import okhttp3.OkHttpClient
import java.io.Closeable

/**
 * MemoryKit — Official Kotlin SDK for the MemoryKit API.
 *
 * Provides access to all MemoryKit API resources: memories, chats, users,
 * webhooks, status, and feedback.
 *
 * ## Quick Start
 *
 * ```kotlin
 * val mk = MemoryKit(apiKey = "ctx_...")
 *
 * // Create a memory
 * val memory = mk.memories.create(
 *     content = "Meeting notes from Q4...",
 *     title = "Q4 Planning Notes",
 *     tags = listOf("planning", "q4")
 * )
 *
 * // Query memories
 * val answer = mk.memories.query(query = "Summarize our Q4 goals")
 *
 * // Don't forget to close when done
 * mk.close()
 * ```
 *
 * @param apiKey Your MemoryKit API key (must start with "ctx_").
 * @param baseUrl Optional custom base URL.
 * @param timeout Request timeout in milliseconds (default: 30000).
 * @param maxRetries Max retry attempts for retryable errors (default: 3).
 * @param httpClient Optional pre-configured OkHttpClient.
 */
class MemoryKit(
    apiKey: String,
    baseUrl: String = MemoryKitConfig.DEFAULT_BASE_URL,
    timeout: Long = MemoryKitConfig.DEFAULT_TIMEOUT_MS,
    maxRetries: Int = MemoryKitConfig.DEFAULT_MAX_RETRIES,
    httpClient: OkHttpClient? = null
) : Closeable {

    /**
     * Create a MemoryKit client from a [MemoryKitConfig].
     */
    constructor(config: MemoryKitConfig) : this(
        apiKey = config.apiKey,
        baseUrl = config.baseUrl,
        timeout = config.timeout,
        maxRetries = config.maxRetries,
        httpClient = config.httpClient
    )

    private val config = MemoryKitConfig(
        apiKey = apiKey,
        baseUrl = baseUrl,
        timeout = timeout,
        maxRetries = maxRetries,
        httpClient = httpClient
    )

    private val http = HttpClient(this.config)

    /**
     * Memories resource — create, list, update, delete, query, search, and stream memories.
     */
    val memories: MemoriesResource = MemoriesResource(http)

    /**
     * Chats resource — create and manage chat sessions with message history.
     */
    val chats: ChatsResource = ChatsResource(http)

    /**
     * Users resource — manage users and user events.
     */
    val users: UsersResource = UsersResource(http)

    /**
     * Webhooks resource — register, list, and test webhooks.
     */
    val webhooks: WebhooksResource = WebhooksResource(http)

    /**
     * Status resource — check API health.
     */
    val status: StatusResource = StatusResource(http)

    /**
     * Feedback resource — submit feedback on query responses.
     */
    val feedback: FeedbackResource = FeedbackResource(http)

    /**
     * Close the underlying HTTP client and release resources.
     *
     * After calling close(), the client should not be used.
     */
    override fun close() {
        http.close()
    }

    companion object {
        /**
         * SDK version.
         */
        const val VERSION = MemoryKitConfig.SDK_VERSION
    }
}
