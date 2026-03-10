# MemoryKit Kotlin SDK

Official Kotlin SDK for MemoryKit RAG API. Coroutines-based, OkHttp + kotlinx.serialization.

## Quick Commands
```bash
./gradlew build         # Build + run tests
./gradlew test          # Run tests only
./gradlew jar           # Build JAR
./gradlew publishToMavenLocal  # Publish to local Maven
```

Note: `gradlew` wrapper not yet generated. Use `gradle` directly or run `gradle wrapper` first.

## Architecture

```
src/main/kotlin/io/memorykit/
├── MemoryKit.kt              # Entry point — holds HttpClient + resource instances
├── MemoryKitConfig.kt        # Configuration (apiKey, baseUrl, timeout, retries)
├── HttpClient.kt             # OkHttp wrapper with retry, auth headers, JSON encoding
├── models/                   # Data models (kotlinx.serialization @Serializable)
│   ├── Memory.kt             # Memory, MemoryList, CreateMemoryRequest, etc.
│   ├── Chat.kt               # Chat, ChatList, ChatMessage, etc.
│   ├── User.kt               # User, UserList
│   ├── Event.kt              # Event, EventList
│   ├── Webhook.kt            # Webhook, WebhookList, WebhookTestResponse
│   ├── Query.kt              # QueryRequest, QueryResponse, QuerySource
│   ├── Search.kt             # SearchRequest, SearchResponse, SearchResult
│   ├── Status.kt             # StatusResponse
│   ├── Feedback.kt           # FeedbackRequest, FeedbackResponse
│   ├── Common.kt             # Shared types (PaginatedList, etc.)
│   └── Errors.kt             # API error models
├── resources/                # One file per API resource
│   ├── MemoriesResource.kt   # CRUD + search + query + stream + upload + reprocess
│   ├── ChatsResource.kt      # CRUD + sendMessage + streamMessage + history
│   ├── UsersResource.kt      # CRUD + events
│   ├── WebhooksResource.kt   # CRUD + test
│   ├── FeedbackResource.kt   # submit()
│   └── StatusResource.kt     # get()
└── sse/
    └── SSEParser.kt          # OkHttp SSE event parser for streaming

```

## Conventions

- **Coroutines**: All resource methods are `suspend fun`, use `kotlinx.coroutines`
- **kotlinx.serialization**: All models are `@Serializable`, JSON naming via `@SerialName` for snake_case
- **OkHttp**: HTTP client with connection pooling, SSE via `okhttp-sse`
- **Resource pattern**: Each resource class takes `HttpClient`, methods map 1:1 to API endpoints
- **Error handling**: `MemoryKitException` hierarchy with statusCode, code, message
- **JVM 8 target**: Compatible with Android 26+ and JVM 8+

## Dependencies

- `kotlinx-coroutines-core:1.8.1`
- `kotlinx-serialization-json:1.7.1`
- `okhttp:4.12.0`
- `okhttp-sse:4.12.0`

## API → SDK Method Mapping

| API Endpoint | SDK Method |
|---|---|
| `POST /v1/memories` | `mk.memories.create()` |
| `GET /v1/memories` | `mk.memories.list()` |
| `GET /v1/memories/:id` | `mk.memories.get()` |
| `PUT /v1/memories/:id` | `mk.memories.update()` |
| `DELETE /v1/memories/:id` | `mk.memories.delete()` |
| `GET /v1/memories/search` | `mk.memories.search()` |
| `POST /v1/memories/query` | `mk.memories.query()` |
| `POST /v1/memories/query/stream` | `mk.memories.stream()` |
| `POST /v1/memories/upload` | `mk.memories.upload()` |
| `POST /v1/memories/:id/reprocess` | `mk.memories.reprocess()` |
| `POST /v1/chats` | `mk.chats.create()` |
| `GET /v1/chats` | `mk.chats.list()` |
| `GET /v1/chats/:id` | `mk.chats.get()` |
| `DELETE /v1/chats/:id` | `mk.chats.delete()` |
| `POST /v1/chats/:id/messages` | `mk.chats.sendMessage()` |
| `POST /v1/chats/:id/messages/stream` | `mk.chats.streamMessage()` |
| `GET /v1/chats/:id/history` | `mk.chats.getHistory()` |
| `POST /v1/users` | `mk.users.upsert()` |
| `GET /v1/users/:id` | `mk.users.get()` |
| `PUT /v1/users/:id` | `mk.users.update()` |
| `DELETE /v1/users/:id` | `mk.users.delete()` |
| `POST /v1/users/:id/events` | `mk.users.createEvent()` |
| `GET /v1/users/:id/events` | `mk.users.listEvents()` |
| `DELETE /v1/users/:id/events/:eid` | `mk.users.deleteEvent()` |
| `POST /v1/webhooks` | `mk.webhooks.create()` |
| `GET /v1/webhooks` | `mk.webhooks.list()` |
| `GET /v1/webhooks/:id` | `mk.webhooks.get()` |
| `DELETE /v1/webhooks/:id` | `mk.webhooks.delete()` |
| `POST /v1/webhooks/:id/test` | `mk.webhooks.test()` |
| `GET /v1/status` | `mk.status.get()` |
| `POST /v1/feedback` | `mk.feedback.submit()` |

## Adding a New Method

1. Add `@Serializable` model classes to `models/`
2. Add `suspend fun` to the resource class in `resources/`
3. Run `gradle build` to verify compilation
4. Update README.md

## Testing (TODO)

Currently 0% test coverage. Test deps configured: kotlin-test, coroutines-test, mockwebserver.
