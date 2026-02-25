package io.memorykit.sse

import io.memorykit.models.ConnectionException
import io.memorykit.models.SSEEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Parses Server-Sent Events (SSE) from an OkHttp Response into a Kotlin Flow.
 *
 * SSE format:
 * ```
 * event: text
 * data: {"content": "Hello"}
 *
 * event: done
 * data: {}
 * ```
 *
 * Each blank line terminates an event. Fields:
 * - `event:` sets the event type
 * - `data:` sets the data payload (multiple data lines are joined with newlines)
 * - `id:` sets the event ID
 * - Lines starting with `:` are comments and are ignored.
 */
internal object SSEParser {

    /**
     * Parse an OkHttp Response body as an SSE stream and emit events as a Flow.
     *
     * The Flow completes when the stream ends or an event with type "done" is received.
     * The Response is closed when the Flow completes or is cancelled.
     */
    fun parse(response: Response): Flow<SSEEvent> = callbackFlow {
        val body = response.body
            ?: throw ConnectionException("Empty response body for SSE stream")

        val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))

        try {
            withContext(Dispatchers.IO) {
                var eventType: String? = null
                var dataLines = mutableListOf<String>()
                var eventId: String? = null

                var line: String? = reader.readLine()
                while (line != null) {
                    when {
                        // Blank line = dispatch event
                        line.isEmpty() -> {
                            if (dataLines.isNotEmpty()) {
                                val event = SSEEvent(
                                    event = eventType ?: "message",
                                    data = dataLines.joinToString("\n"),
                                    id = eventId
                                )
                                trySend(event)

                                // Stop on "done" event
                                if (eventType == "done" || eventType == "error") {
                                    break
                                }
                            }
                            // Reset for next event
                            eventType = null
                            dataLines = mutableListOf()
                            eventId = null
                        }
                        // Comment line
                        line.startsWith(":") -> {
                            // Ignore comments
                        }
                        // Field lines
                        else -> {
                            val colonIndex = line.indexOf(':')
                            if (colonIndex > 0) {
                                val fieldName = line.substring(0, colonIndex)
                                // Value starts after ": " (space is optional per spec)
                                val fieldValue = if (colonIndex + 1 < line.length && line[colonIndex + 1] == ' ') {
                                    line.substring(colonIndex + 2)
                                } else {
                                    line.substring(colonIndex + 1)
                                }

                                when (fieldName) {
                                    "event" -> eventType = fieldValue
                                    "data" -> dataLines.add(fieldValue)
                                    "id" -> eventId = fieldValue
                                    // Ignore unknown fields (retry, etc.)
                                }
                            }
                        }
                    }

                    line = reader.readLine()
                }

                // Dispatch any remaining event data
                if (dataLines.isNotEmpty()) {
                    val event = SSEEvent(
                        event = eventType ?: "message",
                        data = dataLines.joinToString("\n"),
                        id = eventId
                    )
                    trySend(event)
                }
            }
        } catch (e: IOException) {
            // Stream closed or connection error -- close naturally
        } finally {
            close()
        }

        awaitClose {
            try {
                reader.close()
            } catch (_: IOException) { }
            response.close()
        }
    }
}
