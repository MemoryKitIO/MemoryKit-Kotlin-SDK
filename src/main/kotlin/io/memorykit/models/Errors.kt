package io.memorykit.models

import kotlinx.serialization.Serializable

/**
 * Base exception for all MemoryKit API errors.
 */
open class MemoryKitException(
    message: String,
    val statusCode: Int? = null,
    val code: String? = null,
    val requestId: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    override fun toString(): String {
        val parts = mutableListOf("MemoryKitException")
        statusCode?.let { parts.add("status=$it") }
        code?.let { parts.add("code=$it") }
        requestId?.let { parts.add("requestId=$it") }
        parts.add("message=$message")
        return parts.joinToString(", ", prefix = "[", postfix = "]")
    }
}

/**
 * 400 Bad Request - invalid request parameters.
 */
class BadRequestException(
    message: String,
    code: String? = null,
    requestId: String? = null
) : MemoryKitException(message, statusCode = 400, code = code, requestId = requestId)

/**
 * 401 Unauthorized - invalid or missing API key.
 */
class AuthenticationException(
    message: String,
    code: String? = null,
    requestId: String? = null
) : MemoryKitException(message, statusCode = 401, code = code, requestId = requestId)

/**
 * 403 Forbidden - insufficient permissions.
 */
class PermissionException(
    message: String,
    code: String? = null,
    requestId: String? = null
) : MemoryKitException(message, statusCode = 403, code = code, requestId = requestId)

/**
 * 404 Not Found - resource does not exist.
 */
class NotFoundException(
    message: String,
    code: String? = null,
    requestId: String? = null
) : MemoryKitException(message, statusCode = 404, code = code, requestId = requestId)

/**
 * 429 Too Many Requests - rate limit exceeded.
 */
class RateLimitException(
    message: String,
    code: String? = null,
    requestId: String? = null,
    val retryAfter: Long? = null
) : MemoryKitException(message, statusCode = 429, code = code, requestId = requestId)

/**
 * 5xx Server Error - internal server error.
 */
class ServerException(
    message: String,
    statusCode: Int,
    code: String? = null,
    requestId: String? = null
) : MemoryKitException(message, statusCode = statusCode, code = code, requestId = requestId)

/**
 * Connection or network error.
 */
class ConnectionException(
    message: String,
    cause: Throwable? = null
) : MemoryKitException(message, cause = cause)

/**
 * Structured error response from the API.
 */
@Serializable
data class ApiErrorResponse(
    val error: ApiErrorDetail? = null,
    val message: String? = null,
    val code: String? = null,
    val requestId: String? = null
)

@Serializable
data class ApiErrorDetail(
    val message: String? = null,
    val code: String? = null,
    val type: String? = null
)
