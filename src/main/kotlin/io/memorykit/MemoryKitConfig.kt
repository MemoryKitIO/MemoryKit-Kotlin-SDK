package io.memorykit

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Configuration for the MemoryKit client.
 *
 * @property apiKey The API key for authentication. Must start with "ctx_".
 * @property baseUrl The base URL for the MemoryKit API.
 * @property timeout Request timeout in milliseconds.
 * @property maxRetries Maximum number of retries for retryable errors (429, 5xx).
 * @property retryBaseDelay Base delay in milliseconds for exponential backoff.
 * @property retryMaxDelay Maximum delay in milliseconds for exponential backoff.
 * @property httpClient Optional pre-configured OkHttpClient instance.
 */
data class MemoryKitConfig(
    val apiKey: String,
    val baseUrl: String = DEFAULT_BASE_URL,
    val timeout: Long = DEFAULT_TIMEOUT_MS,
    val maxRetries: Int = DEFAULT_MAX_RETRIES,
    val retryBaseDelay: Long = DEFAULT_RETRY_BASE_DELAY_MS,
    val retryMaxDelay: Long = DEFAULT_RETRY_MAX_DELAY_MS,
    val httpClient: OkHttpClient? = null
) {
    init {
        require(apiKey.isNotBlank()) { "API key must not be blank" }
        require(apiKey.startsWith("ctx_")) { "API key must start with 'ctx_'" }
        require(maxRetries >= 0) { "maxRetries must be >= 0" }
        require(timeout > 0) { "timeout must be > 0" }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://api.memorykit.io/v1"
        const val DEFAULT_TIMEOUT_MS = 30_000L
        const val DEFAULT_MAX_RETRIES = 3
        const val DEFAULT_RETRY_BASE_DELAY_MS = 500L
        const val DEFAULT_RETRY_MAX_DELAY_MS = 30_000L
        const val SDK_VERSION = "0.1.0"
        const val USER_AGENT = "memorykit-kotlin/$SDK_VERSION"
    }

    /**
     * Create the OkHttpClient to use, either from the user-provided one or a new default.
     */
    internal fun buildHttpClient(): OkHttpClient {
        return httpClient ?: OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .build()
    }
}
