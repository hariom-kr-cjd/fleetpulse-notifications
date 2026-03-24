plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("io.ktor.plugin") version "2.3.7"
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
}

group = "com.fleetpulse"
version = "1.0.0"

application {
    mainClass.set("com.fleetpulse.notifications.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core-jvm:${property("ktor_version")}")
    implementation("io.ktor:ktor-server-netty-jvm:${property("ktor_version")}")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:${property("ktor_version")}")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:${property("ktor_version")}")
    implementation("io.ktor:ktor-server-cors-jvm:${property("ktor_version")}")
    implementation("io.ktor:ktor-server-auth-jvm:${property("ktor_version")}")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:${property("ktor_version")}")
    implementation("io.ktor:ktor-server-status-pages-jvm:${property("ktor_version")}")

    // Koin DI
    implementation("io.insert-koin:koin-ktor:${property("koin_version")}")

    // MongoDB
    implementation("org.litote.kmongo:kmongo-coroutine-serialization:${property("kmongo_version")}")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm:${property("ktor_version")}")
    testImplementation("io.ktor:ktor-client-content-negotiation:${property("ktor_version")}")
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.insert-koin:koin-test:${property("koin_version")}")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.12.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

koverReport {
    verify {
        rule {
            minBound(75)
        }
    }
}
