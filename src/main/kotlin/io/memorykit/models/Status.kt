package io.memorykit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API status information.
 */
@Serializable
data class StatusResponse(
    val status: String? = null,
    val version: String? = null,
    val uptime: Long? = null,
    val timestamp: String? = null
)
