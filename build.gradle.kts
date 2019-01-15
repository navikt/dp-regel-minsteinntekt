import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    application
    kotlin("jvm") version "1.3.11"
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
    maven("http://packages.confluent.io/maven/")
}

application {
    applicationName = "dp-regel-minsteinntekt"
    mainClassName = "no.nav.dagpenger.regel.minsteinntekt.MinsteinntektRegel"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val kafkaVersion = "2.0.1"
val kotlinLoggingVersion = "1.4.9"
val log4j2Version = "2.11.1"
val jupiterVersion = "5.3.2"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("no.nav.dagpenger:streams:0.2.2-SNAPSHOT")
    implementation("no.nav.dagpenger:events:0.1.9-SNAPSHOT")
    compile("org.apache.kafka:kafka-clients:$kafkaVersion")
    compile("org.apache.kafka:kafka-streams:$kafkaVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    implementation("com.vlkan.log4j2:log4j2-logstash-layout-fatjar:0.15")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
}

application {
    applicationName = "dp-datalaster-inntekt"
    mainClassName = "no.nav.dagpenger.inntekt.Datalaster"
}

spotless {
    kotlin {
        ktlint()
    }
    kotlinGradle {
        target("*.gradle.kts", "additionalScripts/*.gradle.kts")
        ktlint()
    }
}

tasks.withType<Test> {
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}
