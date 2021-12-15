import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    application
    kotlin("jvm") version Kotlin.version
    id(Spotless.spotless) version Spotless.version
    id(Shadow.shadow) version Shadow.version
}

buildscript {
    repositories {
        mavenCentral()
    }
}

apply {
    plugin(Spotless.spotless)
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
}

application {
    applicationName = "dp-regel-minsteinntekt"
    mainClass.set("no.nav.dagpenger.regel.minsteinntekt.ApplicationKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Multi-Release"] = "true" // https://github.com/johnrengelman/shadow/issues/449
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

val kafkaVersion = "2.0.1"
val kotlinLoggingVersion = "1.4.9"
val log4j2Version = "2.11.1"
val jupiterVersion = "5.3.2"
val confluentVersion = "5.0.2"
val prometheusVersion = "0.6.0"
val ktorVersion = "1.2.0"
val moshiVersion = "1.8.0"
val ktorMoshiVersion = "1.0.1"
val orgJsonVersion = "20180813"

dependencies {
    implementation(kotlin("stdlib"))

    implementation(Dagpenger.Streams)
    implementation(Dagpenger.Events)
    implementation(Dagpenger.Grunnbeløp)

    implementation(Moshi.moshi)
    implementation(Moshi.moshiAdapters)
    implementation(Moshi.moshiKotlin)
    implementation(Moshi.moshiKtor)

    implementation(Ulid.ulid)

    implementation(Prometheus.common)
    implementation(Prometheus.hotspot)
    implementation(Prometheus.Nare.prometheus)

    implementation(Log4j2.api)
    implementation(Log4j2.core)
    implementation(Log4j2.slf4j)
    implementation(Log4j2.library("layout-template-json"))

    implementation(Nare.nare)

    implementation(Ktor.serverNetty)

    implementation(Kafka.clients)
    implementation(Kafka.streams)

    implementation(Konfig.konfig)
    implementation(Kotlin.Logging.kotlinLogging)

    implementation("no.finn.unleash:unleash-client-java:3.2.9")

    testImplementation(kotlin("test"))
    testImplementation(Junit5.params)
    testImplementation(Junit5.api)
    testRuntimeOnly(Junit5.engine)

    testImplementation(KoTest.runner)
    testImplementation(KoTest.assertions)
    testImplementation(KoTest.property)

    testImplementation(Kafka.streamTestUtils)
    testImplementation(Wiremock.standalone)
    testImplementation(Mockk.mockk)
}

spotless {
    kotlin {
        ktlint(Ktlint.version)
    }
    kotlinGradle {
        target("*.gradle.kts", "buildSrc/**/*.kt*")
        ktlint(Ktlint.version)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "6.0.1"
}

tasks.named("compileKotlin") {
    dependsOn("spotlessKotlinCheck")
}
