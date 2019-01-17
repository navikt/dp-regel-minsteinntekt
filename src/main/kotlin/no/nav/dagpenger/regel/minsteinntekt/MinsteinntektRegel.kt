package no.nav.dagpenger.regel.minsteinntekt

import mu.KotlinLogging
import no.nav.dagpenger.events.avro.MinsteinntektResultat
import no.nav.dagpenger.events.avro.RegelType
import no.nav.dagpenger.events.avro.Vilkår
import no.nav.dagpenger.events.getRegel
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.Service
import no.nav.dagpenger.streams.Topics
import no.nav.dagpenger.streams.consumeTopic
import no.nav.dagpenger.streams.streamConfig
import no.nav.dagpenger.streams.toTopic
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import java.util.Properties

private val LOGGER = KotlinLogging.logger {}

class MinsteinntektRegel(val env: Environment) : Service() {
    override val SERVICE_APP_ID: String = "dagpenger-regel-minsteinntekt"

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val service = MinsteinntektRegel(Environment())
            service.start()
        }
    }

    override fun setupStreams(): KafkaStreams {
        LOGGER.info { "Initiating start of $SERVICE_APP_ID" }
        val builder = StreamsBuilder()

        val inngåendeJournalposter = builder.consumeTopic(Topics.VILKÅR_EVENT, env.schemaRegistryUrl)

        inngåendeJournalposter
                .peek { key, value -> LOGGER.info("Processing ${value.javaClass} with key $key") }
                .filter { _, vilkår -> shouldBeProcessed(vilkår) }
                .mapValues(this::addRegelresultat)
                .peek { key, value -> LOGGER.info("Producing ${value.javaClass} with key $key") }
                .toTopic(Topics.VILKÅR_EVENT, env.schemaRegistryUrl)

        return KafkaStreams(builder.build(), this.getConfig())
    }

    override fun getConfig(): Properties {
        val props = streamConfig(
                appId = SERVICE_APP_ID,
                bootStapServerUrl = env.bootstrapServersUrl,
                credential = KafkaCredential(env.username, env.password)
        )
        return props
    }

    private fun addRegelresultat(vilkår: Vilkår): Vilkår {
        val regel = vilkår.getRegel(RegelType.FIRE_FIRE)
        regel!!.setResultat(MinsteinntektResultat.newBuilder().apply {
            oppfyllerKravetTilMinsteArbeidsinntekt = true
            periodeAntallUker = 104
        })
        return vilkår
    }
}

fun shouldBeProcessed(vilkår: Vilkår): Boolean {
    val regel = vilkår.getRegel(RegelType.FIRE_FIRE) ?: return false

    return regel.getResultat() == null && vilkår.getInntekter() != null
}

class MinsteinntektRegelException(override val message: String) : RuntimeException(message)