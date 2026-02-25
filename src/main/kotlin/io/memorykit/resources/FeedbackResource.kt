package io.memorykit.resources

import io.memorykit.HttpClient
import io.memorykit.models.FeedbackRequest
import io.memorykit.models.FeedbackResponse

/**
 * Resource for submitting feedback on query responses.
 */
class FeedbackResource internal constructor(private val http: HttpClient) {

    /**
     * Submit feedback for a query response.
     *
     * @param requestId The request ID from the query response.
     * @param rating Rating value (e.g., 1-5).
     * @param comment Optional feedback comment.
     * @return The [FeedbackResponse].
     */
    suspend fun submit(
        requestId: String,
        rating: Int,
        comment: String? = null
    ): FeedbackResponse {
        val request = FeedbackRequest(
            requestId = requestId,
            rating = rating,
            comment = comment
        )
        val body = http.json.encodeToString(FeedbackRequest.serializer(), request)
        val response = http.post("/feedback", body)
        return http.json.decodeFromString(FeedbackResponse.serializer(), response)
    }
}
