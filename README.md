# FleetPulse Notifications

Notification microservice for Fleet & Driver Management System.

![CI](https://github.com/hariom-kr-cjd/fleetpulse-notifications/actions/workflows/ci.yml/badge.svg)

## Tech Stack
- **Language:** Kotlin
- **Framework:** Ktor
- **Database:** MongoDB / KMongo Coroutine
- **DI:** Koin
- **Testing:** Kotest + Embedded MongoDB
- **Build:** Gradle 8.5 / Java 17

## Features
- CRUD for notifications (create, list, get, delete)
- Pagination with configurable page size
- Mark as read / mark all as read
- Unread count endpoint
- Bulk create for system-generated notifications
- Dual auth: JWT (user endpoints) + Bearer internal key (Node.js API → Kotlin)

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /api/v1/notifications | JWT | List notifications (paginated) |
| GET | /api/v1/notifications/:id | JWT | Get single notification |
| PATCH | /api/v1/notifications/:id/read | JWT | Mark as read |
| PATCH | /api/v1/notifications/read-all | JWT | Mark all as read |
| GET | /api/v1/notifications/unread-count | JWT | Get unread count |
| POST | /api/v1/notifications | Internal | Create notification |
| POST | /api/v1/notifications/bulk | Internal | Bulk create |

## Getting Started

```bash
# Requires Java 17 (install via SDKMAN)
sdk use java 17.0.13-tem

# Run tests
./gradlew test

# Build fat JAR
./gradlew buildFatJar

# Run
java -jar build/libs/fleetpulse-notifications-all.jar
```

## Related Repos
- [fleetpulse-api](https://github.com/hariom-kr-cjd/fleetpulse-api) — Node.js REST API
- [fleetpulse-ui](https://github.com/hariom-kr-cjd/fleetpulse-ui) — Angular 15 frontend
- [fleetpulse-infra](https://github.com/hariom-kr-cjd/fleetpulse-infra) — Docker orchestration
