package io.memorykit.resources

import io.memorykit.HttpClient
import io.memorykit.models.*
import kotlinx.serialization.json.JsonObject

/**
 * Resource for managing users and user events.
 */
class UsersResource internal constructor(private val http: HttpClient) {

    /**
     * Create or update a user (upsert).
     *
     * @param id The user ID (required).
     * @param email Optional email.
     * @param name Optional name.
     * @param metadata Optional metadata.
     * @return The created/updated [User].
     */
    suspend fun upsert(
        id: String,
        email: String? = null,
        name: String? = null,
        metadata: JsonObject? = null
    ): User {
        val request = UpsertUserRequest(
            id = id,
            email = email,
            name = name,
            metadata = metadata
        )
        val body = http.json.encodeToString(UpsertUserRequest.serializer(), request)
        val response = http.post("/users", body)
        return http.json.decodeFromString(User.serializer(), response)
    }

    /**
     * Get a user by ID.
     *
     * @param id The user ID.
     * @return The [User].
     */
    suspend fun get(id: String): User {
        val response = http.get("/users/$id")
        return http.json.decodeFromString(User.serializer(), response)
    }

    /**
     * Update a user.
     *
     * @param id The user ID.
     * @param email New email.
     * @param name New name.
     * @param metadata New metadata.
     * @return The updated [User].
     */
    suspend fun update(
        id: String,
        email: String? = null,
        name: String? = null,
        metadata: JsonObject? = null
    ): User {
        val request = UpdateUserRequest(
            email = email,
            name = name,
            metadata = metadata
        )
        val body = http.json.encodeToString(UpdateUserRequest.serializer(), request)
        val response = http.put("/users/$id", body)
        return http.json.decodeFromString(User.serializer(), response)
    }

    /**
     * Delete a user.
     *
     * @param id The user ID.
     * @param cascade If true, delete all associated memories and events.
     */
    suspend fun delete(id: String, cascade: Boolean = false) {
        val params = if (cascade) mapOf("cascade" to "true") else emptyMap()
        http.delete("/users/$id", params)
    }

    // --- User Events ---

    /**
     * Create an event for a user.
     *
     * @param userId The user ID.
     * @param type The event type (required).
     * @param data Optional event data.
     * @return The created [Event].
     */
    suspend fun createEvent(
        userId: String,
        type: String,
        data: JsonObject? = null
    ): Event {
        val request = CreateEventRequest(type = type, data = data)
        val body = http.json.encodeToString(CreateEventRequest.serializer(), request)
        val response = http.post("/users/$userId/events", body)
        return http.json.decodeFromString(Event.serializer(), response)
    }

    /**
     * List events for a user.
     *
     * @param userId The user ID.
     * @param limit Maximum number of results (default 20).
     * @param type Filter by event type.
     * @return A [ListResponse] containing events.
     */
    suspend fun listEvents(
        userId: String,
        limit: Int? = null,
        type: String? = null
    ): ListResponse<Event> {
        val params = mapOf(
            "limit" to limit?.toString(),
            "type" to type
        )
        val response = http.get("/users/$userId/events", params)
        return http.json.decodeFromString(ListResponse.serializer(Event.serializer()), response)
    }

    /**
     * Delete an event for a user.
     *
     * @param userId The user ID.
     * @param eventId The event ID.
     */
    suspend fun deleteEvent(userId: String, eventId: String) {
        http.delete("/users/$userId/events/$eventId")
    }
}
