package no.nav.dagpenger.regel.minsteinntekt

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.booleanType
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.streams.PacketDeserializer
import no.nav.dagpenger.streams.PacketSerializer
import no.nav.dagpenger.streams.Topic
import org.apache.kafka.common.serialization.Serdes

private val localProperties = ConfigurationMap(
    mapOf(
        "KAFKA_BROKERS" to "localhost:9092",
        "application.profile" to Profile.LOCAL.toString(),
        "application.httpPort" to "8080",
        "inntekt.gprc.address" to "localhost",
        "inntekt.gprc.api.key" to "apikey",
        "inntekt.gprc.api.secret" to "secret"
    )
)
private val devProperties = ConfigurationMap(
    mapOf(
        "application.profile" to Profile.DEV.toString(),
        "application.httpPort" to "8080",
        "feature.gjustering" to false.toString(),
        "feature.koronaperiode2" to true.toString(),
        "feature.gjustering" to false.toString(),
        "inntekt.gprc.address" to "dp-inntekt-api-grpc.teamdagpenger.svc.nais.local"
    )
)
private val prodProperties = ConfigurationMap(
    mapOf(
        "kafka.reset.policy" to "earliest",
        "application.profile" to Profile.PROD.toString(),
        "application.httpPort" to "8080",
        "feature.koronaperiode2" to true.toString(),
        "inntekt.gprc.address" to "dp-inntekt-api-grpc.teamdagpenger.svc.nais.local"
    )
)

private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
    "dev-fss" -> systemProperties() overriding EnvironmentVariables overriding devProperties
    "prod-fss" -> systemProperties() overriding EnvironmentVariables overriding prodProperties
    else -> {
        systemProperties() overriding EnvironmentVariables overriding localProperties
    }
}

val REGEL_TOPIC = Topic(
    "teamdagpenger.regel.v1",
    keySerde = Serdes.String(),
    valueSerde = Serdes.serdeFrom(PacketSerializer(), PacketDeserializer())
)

data class Configuration(
    val kafka: Kafka = Kafka(),
    val application: Application = Application(),
    val features: Features = Features(),
    val regelTopic: Topic<String, Packet> = REGEL_TOPIC
) {
    data class Kafka(
        val aivenBrokers: String = config()[Key("KAFKA_BROKERS", stringType)]
    )

    data class Application(
        val id: String = config().getOrElse(Key("application.id", stringType), "dagpenger-regel-minsteinntekt"),
        val profile: Profile = config()[Key("application.profile", stringType)].let { Profile.valueOf(it) },
        val httpPort: Int = config()[Key("application.httpPort", intType)],
        val inntektGprcAddress: String = config()[Key("inntekt.gprc.address", stringType)],
        val inntektGprcApiKey: String = config()[Key("inntekt.gprc.api.key", stringType)],
        val inntektGprcApiSecret: String = config()[Key("inntekt.gprc.api.secret", stringType)]
    )

    class Features {
        fun gjustering() = config().getOrElse(Key("feature.gjustering", booleanType), false)
        fun koronaperiode2() = config().getOrElse(Key("feature.koronaperiode2", booleanType), false)
    }
}

enum class Profile {
    LOCAL, DEV, PROD
}
