# MemoryKit Kotlin SDK

Official Kotlin/JVM SDK for [MemoryKit](https://memorykit.io) — memory infrastructure for AI applications.

[![Maven Central](https://img.shields.io/maven-central/v/io.memorykit/memorykit)](https://central.sonatype.com/artifact/io.memorykit/memorykit)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## Features

- Full coverage of the MemoryKit API (memories, users, webhooks, feedback)
- Kotlin coroutines (`suspend` functions) for all async operations
- Strongly typed request/response models with `kotlinx.serialization`
- OkHttp 4.x under the hood
- Automatic retry with exponential backoff on 429 and 5xx errors
- Android compatible (minSdk 21+)

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.memorykit:memorykit:0.1.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.memorykit:memorykit:0.1.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.memorykit</groupId>
    <artifactId>memorykit</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quick Start

```kotlin
import io.memorykit.MemoryKit

val mk = MemoryKit(apiKey = "ctx_your_api_key")

// Create a memory
val memory = mk.memories.create(
    content = "Meeting notes from Q4 planning session...",
    title = "Q4 Planning Notes",
    tags = listOf("planning", "q4"),
    userId = "user_123"
)

// Search memories
val results = mk.memories.search(query = "Q4 goals", precision = SearchPrecision.HIGH)
results.results.forEach { println("${it.title}: ${it.score}") }

// Close when done
mk.close()
```

## API Reference

### Initialize

```kotlin
// Simple
val mk = MemoryKit(apiKey = "ctx_...")

// With options
val mk = MemoryKit(
    apiKey = "ctx_...",
    baseUrl = "https://api.memorykit.io/v1",  // default
    timeout = 30_000L,                         // 30 seconds
    maxRetries = 3                             // retry on 429/5xx
)

// With config object
val config = MemoryKitConfig(
    apiKey = "ctx_...",
    timeout = 60_000L,
    maxRetries = 5
)
val mk = MemoryKit(config)
```

### Memories

```kotlin
// Create
val memory = mk.memories.create(
    content = "Meeting notes from Q4...",
    title = "Q4 Planning Notes",
    tags = listOf("planning", "q4"),
    userId = "user_123"
)

// Batch ingest
val batch = mk.memories.batchIngest(
    items = listOf(
        CreateMemoryRequest(content = "First memory"),
        CreateMemoryRequest(content = "Second memory")
    ),
    defaults = BatchDefaults(tags = listOf("batch"))
)

// List with pagination
val list = mk.memories.list(limit = 20, status = "completed")
println("Has more: ${list.hasMore}")

// Get by ID
val mem = mk.memories.get("mem_abc123")

// Update
val updated = mk.memories.update("mem_abc123", title = "New Title")

// Delete
mk.memories.delete("mem_abc123")
```

### Hybrid Search

```kotlin
val results = mk.memories.search(
    query = "quarterly revenue targets",
    precision = SearchPrecision.HIGH,
    limit = 10,
    type = "meeting_notes",
    tags = "planning,q4",
    createdAfter = "2025-01-01T00:00:00Z"
)
results.results.forEach { result ->
    println("${result.title}: ${result.score}")
}
```

### Users

```kotlin
// Upsert user
val user = mk.users.upsert(
    id = "user_123",
    name = "Alice",
    email = "alice@example.com",
    metadata = buildJsonObject { put("plan", "pro") }
)

// Get user
val fetched = mk.users.get("user_123")

// Update user
val updated = mk.users.update("user_123", name = "Alice Smith")

// Delete user (with cascade to remove associated data)
mk.users.delete("user_123", cascade = true)
```

### User Events

```kotlin
// Create event
val event = mk.users.createEvent(
    userId = "user_123",
    type = "page_view",
    data = buildJsonObject { put("page", "/settings") }
)

// List events
val events = mk.users.listEvents("user_123", limit = 50, type = "page_view")

// Delete event
mk.users.deleteEvent("user_123", event.id!!)
```

### Webhooks

```kotlin
// Register
val webhook = mk.webhooks.create(
    url = "https://example.com/webhook",
    events = listOf("memory.created", "memory.updated")
)

// List
val webhooks = mk.webhooks.list()

// Get
val wh = mk.webhooks.get(webhook.id)

// Test
val testResult = mk.webhooks.test(webhook.id)
println("Test success: ${testResult.success}")

// Delete
mk.webhooks.delete(webhook.id)
```

### Status

```kotlin
val status = mk.status.get()
println("API status: ${status.status}")
```

### Feedback

```kotlin
mk.feedback.submit(
    requestId = "req_abc123",
    rating = 5,
    comment = "Very accurate answer"
)
```

## Error Handling

All API errors are thrown as `MemoryKitException` subclasses:

```kotlin
import io.memorykit.models.*

try {
    mk.memories.get("nonexistent")
} catch (e: NotFoundException) {
    println("Not found: ${e.message}")
} catch (e: AuthenticationException) {
    println("Auth error: ${e.message}")
} catch (e: RateLimitException) {
    println("Rate limited. Retry after: ${e.retryAfter}s")
} catch (e: BadRequestException) {
    println("Bad request: ${e.message}")
} catch (e: ServerException) {
    println("Server error (${e.statusCode}): ${e.message}")
} catch (e: ConnectionException) {
    println("Network error: ${e.message}")
} catch (e: MemoryKitException) {
    println("API error (${e.statusCode}): ${e.message}")
}
```

Exception hierarchy:

| Exception | Status Code | Description |
|-----------|-------------|-------------|
| `BadRequestException` | 400 | Invalid request parameters |
| `AuthenticationException` | 401 | Invalid or missing API key |
| `PermissionException` | 403 | Insufficient permissions |
| `NotFoundException` | 404 | Resource not found |
| `RateLimitException` | 429 | Rate limit exceeded |
| `ServerException` | 5xx | Server error |
| `ConnectionException` | - | Network/connection error |

## Retry Behavior

The SDK automatically retries on:
- **429 Too Many Requests**: Respects `Retry-After` header when present
- **5xx Server Errors**: Uses exponential backoff

Default: up to 3 retries with 500ms base delay and jitter.

Configure via constructor:

```kotlin
val mk = MemoryKit(
    apiKey = "ctx_...",
    maxRetries = 5  // 0 to disable retries
)
```

## Android

The SDK is compatible with Android (minSdk 21+). Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.memorykit:memorykit:0.1.0")
}
```

Use with a CoroutineScope (e.g., `viewModelScope`):

```kotlin
class MyViewModel : ViewModel() {
    private val mk = MemoryKit(apiKey = "ctx_...")

    fun searchMemories(query: String) {
        viewModelScope.launch {
            try {
                val results = mk.memories.search(query = query)
                // Update UI state with results.results
            } catch (e: MemoryKitException) {
                // Handle error
            }
        }
    }

    override fun onCleared() {
        mk.close()
    }
}
```

## Requirements

- Kotlin 1.9+
- Java 8+ (JVM target)
- Android minSdk 21+ (if targeting Android)

## Dependencies

- `org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1`
- `org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1`
- `com.squareup.okhttp3:okhttp:4.12.0`
- `com.squareup.okhttp3:okhttp-sse:4.12.0`

## License

MIT - see [LICENSE](LICENSE) for details.
