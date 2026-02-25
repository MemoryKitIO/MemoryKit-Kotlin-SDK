package io.memorykit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for submitting feedback.
 */
@Serializable
data class FeedbackRequest(
    @SerialName("request_id")
    val requestId: String,
    val rating: Int,
    val comment: String? = null
)

/**
 * Response from submitting feedback.
 */
@Serializable
data class FeedbackResponse(
    val success: Boolean? = null,
    val message: String? = null
)
