import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    application
    kotlin("jvm") version "1.3.21"
    id("com.diffplug.gradle.spotless") version "3.13.0"
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

buildscript {
    repositories {
        mavenCentral()
    }
}

apply {
    plugin("com.diffplug.gradle.spotless")
}

repositories {
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("http://packages.confluent.io/maven/")
    maven("https://jitpack.io")
}

application {
    applicationName = "dp-regel-minsteinntekt"
    mainClassName = "no.nav.dagpenger.regel.minsteinntekt.MinsteinntektKt"
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
    implementation("com.github.navikt:dagpenger-streams:2019.06.17-12.21.e9270cb15ca1")
    implementation("com.github.navikt.dp-biblioteker:grunnbelop:2019.06.04-12.10.8df531d0e3b3")
    implementation("com.github.navikt:dagpenger-events:2019.06.12-14.01.4b1e1a663635")

    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.ryanharter.ktor:ktor-moshi:$ktorMoshiVersion")

    implementation("no.nav:nare:768ae37")
    implementation("no.nav:nare-prometheus:0b41ab4")

    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.2.0")

    compile("io.prometheus:simpleclient_common:$prometheusVersion")
    compile("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_log4j2:$prometheusVersion")

    compile("io.ktor:ktor-server-netty:$ktorVersion")

    compile("org.apache.kafka:kafka-clients:$kafkaVersion")
    compile("org.apache.kafka:kafka-streams:$kafkaVersion")

    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    implementation("com.vlkan.log4j2:log4j2-logstash-layout-fatjar:0.15")

    implementation("org.json:json:$orgJsonVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testImplementation("no.nav:kafka-embedded-env:2.0.2")
    testImplementation("org.apache.kafka:kafka-streams-test-utils:$kafkaVersion")
}

spotless {
    kotlin {
        ktlint("0.31.0")
    }
    kotlinGradle {
        target("*.gradle.kts", "additionalScripts/*.gradle.kts")
        ktlint("0.31.0")
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
    gradleVersion = "5.0"
}
