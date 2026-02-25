package io.memorykit

import io.memorykit.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.min
import kotlin.math.pow

/**
 * Internal HTTP client that wraps OkHttp with authentication, serialization,
 * error handling, and automatic retry logic.
 */
internal class HttpClient(private val config: MemoryKitConfig) {

    private val client: OkHttpClient = config.buildHttpClient()

    internal val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
        isLenient = true
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    /**
     * Perform a GET request.
     */
    suspend fun get(
        path: String,
        queryParams: Map<String, String?> = emptyMap()
    ): String {
        val url = buildUrl(path, queryParams)
        val request = Request.Builder()
            .url(url)
            .get()
            .applyHeaders()
            .build()
        return executeWithRetry(request)
    }

    /**
     * Perform a POST request with a JSON body.
     */
    suspend fun post(
        path: String,
        body: String? = null,
        queryParams: Map<String, String?> = emptyMap()
    ): String {
        val url = buildUrl(path, queryParams)
        val requestBody = (body ?: "{}").toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .applyHeaders()
            .build()
        return executeWithRetry(request)
    }

    /**
     * Perform a PUT request with a JSON body.
     */
    suspend fun put(
        path: String,
        body: String,
        queryParams: Map<String, String?> = emptyMap()
    ): String {
        val url = buildUrl(path, queryParams)
        val requestBody = body.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .applyHeaders()
            .build()
        return executeWithRetry(request)
    }

    /**
     * Perform a DELETE request.
     */
    suspend fun delete(
        path: String,
        queryParams: Map<String, String?> = emptyMap()
    ) {
        val url = buildUrl(path, queryParams)
        val request = Request.Builder()
            .url(url)
            .delete()
            .applyHeaders()
            .build()
        executeWithRetry(request, allowEmpty = true)
    }

    /**
     * Perform a POST request and return the raw OkHttp Response for SSE streaming.
     * This does NOT use retry logic since the caller handles the stream.
     */
    suspend fun postForStream(
        path: String,
        body: String
    ): Response {
        val url = buildUrl(path)
        val requestBody = body.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .applyHeaders()
            .header("Accept", "text/event-stream")
            .build()
        return executeRaw(request)
    }

    /**
     * Execute the request with automatic retry on 429 and 5xx.
     */
    private suspend fun executeWithRetry(
        request: Request,
        allowEmpty: Boolean = false
    ): String {
        var lastException: Exception? = null

        for (attempt in 0..config.maxRetries) {
            try {
                val response = executeRaw(request)
                val code = response.code
                val bodyString = response.body?.string() ?: ""

                when {
                    code in 200..299 -> return bodyString
                    code == 429 || code in 500..599 -> {
                        response.close()
                        if (attempt < config.maxRetries) {
                            val retryAfter = response.header("Retry-After")?.toLongOrNull()
                            val delayMs = retryAfter?.times(1000)
                                ?: calculateBackoff(attempt)
                            delay(delayMs)
                            lastException = mapError(code, bodyString)
                            continue
                        }
                        throw mapError(code, bodyString)
                    }
                    else -> {
                        response.close()
                        throw mapError(code, bodyString)
                    }
                }
            } catch (e: MemoryKitException) {
                if (e is RateLimitException || e is ServerException) {
                    if (attempt < config.maxRetries) {
                        lastException = e
                        val delayMs = if (e is RateLimitException && e.retryAfter != null) {
                            e.retryAfter * 1000
                        } else {
                            calculateBackoff(attempt)
                        }
                        delay(delayMs)
                        continue
                    }
                }
                throw e
            } catch (e: IOException) {
                if (attempt < config.maxRetries) {
                    lastException = ConnectionException("Connection error: ${e.message}", e)
                    delay(calculateBackoff(attempt))
                    continue
                }
                throw ConnectionException("Connection error: ${e.message}", e)
            }
        }

        throw lastException ?: ConnectionException("Request failed after ${config.maxRetries} retries")
    }

    /**
     * Execute the request and return the raw Response.
     */
    private suspend fun executeRaw(request: Request): Response {
        return suspendCancellableCoroutine { continuation ->
            val call = client.newCall(request)

            continuation.invokeOnCancellation {
                call.cancel()
            }

            call.enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }

                override fun onFailure(call: Call, e: IOException) {
                    if (!continuation.isCancelled) {
                        continuation.resumeWithException(
                            ConnectionException("Connection failed: ${e.message}", e)
                        )
                    }
                }
            })
        }
    }

    /**
     * Map an HTTP error code + body to a typed exception.
     */
    private fun mapError(statusCode: Int, body: String): MemoryKitException {
        val errorMessage: String
        val errorCode: String?
        val requestId: String?

        try {
            val apiError = json.decodeFromString<ApiErrorResponse>(body)
            errorMessage = apiError.error?.message
                ?: apiError.message
                ?: "Request failed with status $statusCode"
            errorCode = apiError.error?.code ?: apiError.code
            requestId = apiError.requestId
        } catch (_: Exception) {
            return MemoryKitException(
                message = body.ifBlank { "Request failed with status $statusCode" },
                statusCode = statusCode
            )
        }

        return when (statusCode) {
            400 -> BadRequestException(errorMessage, errorCode, requestId)
            401 -> AuthenticationException(errorMessage, errorCode, requestId)
            403 -> PermissionException(errorMessage, errorCode, requestId)
            404 -> NotFoundException(errorMessage, errorCode, requestId)
            429 -> RateLimitException(errorMessage, errorCode, requestId)
            in 500..599 -> ServerException(errorMessage, statusCode, errorCode, requestId)
            else -> MemoryKitException(errorMessage, statusCode, errorCode, requestId)
        }
    }

    /**
     * Calculate exponential backoff delay with jitter.
     */
    private fun calculateBackoff(attempt: Int): Long {
        val exponentialDelay = config.retryBaseDelay * 2.0.pow(attempt.toDouble()).toLong()
        val capped = min(exponentialDelay, config.retryMaxDelay)
        // Add jitter: 50% to 100% of the calculated delay
        val jitter = (capped * 0.5 * Math.random()).toLong()
        return capped / 2 + jitter
    }

    /**
     * Build the full URL from path and query parameters.
     */
    private fun buildUrl(path: String, queryParams: Map<String, String?> = emptyMap()): HttpUrl {
        val baseUrl = config.baseUrl.trimEnd('/')
        val fullPath = if (path.startsWith("/")) path else "/$path"

        val urlBuilder = HttpUrl.Builder()

        // Parse the base URL
        val parsed = HttpUrl.parse("$baseUrl$fullPath")
            ?: throw IllegalArgumentException("Invalid URL: $baseUrl$fullPath")

        val builder = parsed.newBuilder()

        // Add query parameters, skipping null values
        queryParams.forEach { (key, value) ->
            if (value != null) {
                builder.addQueryParameter(key, value)
            }
        }

        return builder.build()
    }

    /**
     * Apply standard headers to the request.
     */
    private fun Request.Builder.applyHeaders(): Request.Builder {
        return this
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Content-Type", "application/json")
            .header("User-Agent", MemoryKitConfig.USER_AGENT)
            .header("Accept", "application/json")
    }

    /**
     * Close the underlying OkHttpClient.
     */
    fun close() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }
}
