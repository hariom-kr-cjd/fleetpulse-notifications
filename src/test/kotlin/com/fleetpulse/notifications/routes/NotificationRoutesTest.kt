package com.fleetpulse.notifications.routes

import com.fleetpulse.notifications.EmbeddedMongo
import com.fleetpulse.notifications.TEST_INTERNAL_KEY
import com.fleetpulse.notifications.config.configureAuth
import com.fleetpulse.notifications.di.appModule
import com.fleetpulse.notifications.generateTestJwt
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.koin.core.context.stopKoin
import org.koin.ktor.plugin.Koin

class NotificationRoutesTest : FunSpec({
    var running: TransitionWalker.ReachedState<RunningMongodProcess>? = null
    var mongoUri: String = ""

    beforeSpec {
        running = EmbeddedMongo.start()
        mongoUri = EmbeddedMongo.getConnectionString(running!!)
    }

    afterSpec {
        running?.close()
    }

    afterTest {
        stopKoin()
    }

    fun ApplicationTestBuilder.configureTestApp() {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "mongo.uri" to mongoUri,
                "auth.jwt_secret" to "default-secret",
                "auth.internal_api_key" to "default-key",
                "cors.allowed_origins" to "http://localhost:4200"
            )
        }
        application {
            install(Koin) {
                modules(appModule(mongoUri))
            }
            install(ContentNegotiation) {
                json(Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys = true })
            }
            configureAuth()
            routing {
                healthRoutes()
                notificationRoutes()
            }
        }
    }

    test("GET /health should return ok") {
        testApplication {
            configureTestApp()
            val response = client.get("/health")
            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText() shouldContain "ok"
        }
    }

    test("POST /api/v1/notifications/send with internal key should create notification") {
        testApplication {
            configureTestApp()
            val response = client.post("/api/v1/notifications/send") {
                header(HttpHeaders.Authorization, "Bearer $TEST_INTERNAL_KEY")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"userId":"u1","type":"trip_assigned","title":"New","message":"Go"}""")
            }
            response.status shouldBe HttpStatusCode.Created
            response.bodyAsText() shouldContain "New"
        }
    }

    test("POST /api/v1/notifications/send without key should fail") {
        testApplication {
            configureTestApp()
            val response = client.post("/api/v1/notifications/send") {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"userId":"u1","type":"general","title":"T","message":"M"}""")
            }
            response.status shouldBe HttpStatusCode.Unauthorized
        }
    }

    test("GET /api/v1/notifications with JWT should return user notifications") {
        testApplication {
            configureTestApp()
            val userId = "routeTestUser"
            val jwt = generateTestJwt(userId)

            client.post("/api/v1/notifications/send") {
                header(HttpHeaders.Authorization, "Bearer $TEST_INTERNAL_KEY")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"userId":"$userId","type":"general","title":"Hello","message":"World"}""")
            }

            val response = client.get("/api/v1/notifications") {
                header(HttpHeaders.Authorization, "Bearer $jwt")
            }
            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText() shouldContain "Hello"
        }
    }

    test("GET /api/v1/notifications without JWT should fail") {
        testApplication {
            configureTestApp()
            val response = client.get("/api/v1/notifications")
            response.status shouldBe HttpStatusCode.Unauthorized
        }
    }

    test("GET /api/v1/notifications/unread-count should return count") {
        testApplication {
            configureTestApp()
            val userId = "unreadCountUser"
            val jwt = generateTestJwt(userId)

            client.post("/api/v1/notifications/send") {
                header(HttpHeaders.Authorization, "Bearer $TEST_INTERNAL_KEY")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"userId":"$userId","type":"general","title":"T","message":"M"}""")
            }

            val response = client.get("/api/v1/notifications/unread-count") {
                header(HttpHeaders.Authorization, "Bearer $jwt")
            }
            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText() shouldContain "\"count\""
        }
    }

    test("PATCH /api/v1/notifications/read-all should mark all as read") {
        testApplication {
            configureTestApp()
            val userId = "readAllRouteUser"
            val jwt = generateTestJwt(userId)

            repeat(2) {
                client.post("/api/v1/notifications/send") {
                    header(HttpHeaders.Authorization, "Bearer $TEST_INTERNAL_KEY")
                    header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"userId":"$userId","type":"general","title":"T$it","message":"M"}""")
                }
            }

            val response = client.patch("/api/v1/notifications/read-all") {
                header(HttpHeaders.Authorization, "Bearer $jwt")
            }
            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText() shouldContain "marked"
        }
    }
})
