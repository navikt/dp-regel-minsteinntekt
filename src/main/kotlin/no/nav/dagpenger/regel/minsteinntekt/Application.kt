package no.nav.dagpenger.regel.minsteinntekt

import io.prometheus.client.CollectorRegistry
import java.net.URI
import no.nav.NarePrometheus
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.Problem
import no.nav.dagpenger.inntekt.rpc.InntektHenterWrapper
import no.nav.dagpenger.ktor.auth.ApiKeyVerifier
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.INNTEKT
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.MINSTEINNTEKT_RESULTAT
import no.nav.dagpenger.streams.HealthCheck
import no.nav.dagpenger.streams.HealthStatus
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.streamConfig
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import org.apache.kafka.streams.kstream.Predicate

internal val narePrometheus = NarePrometheus(CollectorRegistry.defaultRegistry)
val config = Configuration()

fun main() {
    val service = Application(config, RapidHealthCheck as HealthCheck)
    service.start()

    val apiKeyVerifier = ApiKeyVerifier(config.application.inntektGprcApiSecret)
    val apiKey = apiKeyVerifier.generate(config.application.inntektGprcApiKey)
    val inntektClient = InntektHenterWrapper(
        serveraddress = config.application.inntektGprcAddress,
        apiKey = apiKey
    )

    Runtime.getRuntime().addShutdownHook(Thread {
        inntektClient.close()
    })

    RapidApplication.create(
        Configuration().rapidApplication
    ).apply {
        LøsningService(
            rapidsConnection = this,
            inntektHenter = inntektClient
        )
    }.also {
        it.register(RapidHealthCheck)
    }.start()
}

class Application(private val configuration: Configuration, private val healthCheck: HealthCheck) : River(configuration.behovTopic) {
    override val SERVICE_APP_ID: String = configuration.application.id
    override val HTTP_PORT: Int = configuration.application.httpPort
    override val healthChecks: List<HealthCheck> = listOf(healthCheck)

    companion object {
        const val BEREGNINGSDATO_GAMMEL_SKRIVEMÅTE = "beregningsDato"
    }

    override fun getConfig() = streamConfig(
        appId = SERVICE_APP_ID,
        bootStapServerUrl = configuration.kafka.brokers,
        credential = configuration.kafka.credential()
    )

    override fun filterPredicates(): List<Predicate<String, Packet>> {
        return listOf(
            Predicate { _, packet -> !packet.hasField(MINSTEINNTEKT_RESULTAT) },
            Predicate { _, packet -> packet.hasField(INNTEKT) },
            Predicate { _, packet -> packet.hasField(BEREGNINGSDATO_GAMMEL_SKRIVEMÅTE) }
        )
    }

    override fun onPacket(packet: Packet): Packet {
        val packetMedLøsning = løsFor(packet)
        return packetMedLøsning
    }

    private fun løsFor(packet: Packet): Packet {
        val fakta = packetToFakta(packet)

        val evaluering: Evaluering = if (fakta.beregningsdato.erKoronaPeriode()) {
            narePrometheus.tellEvaluering { kravTilMinsteinntektKorona.evaluer(fakta) }
        } else {
            narePrometheus.tellEvaluering { kravTilMinsteinntekt.evaluer(fakta) }
        }

        val resultat = MinsteinntektSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            Minsteinntekt.REGELIDENTIFIKATOR,
            evaluering.resultat == Resultat.JA,
            evaluering.finnRegelBrukt()
        )

        packet.putValue(Minsteinntekt.MINSTEINNTEKT_NARE_EVALUERING, Minsteinntekt.jsonAdapterEvaluering.toJson(evaluering))
        packet.putValue(MINSTEINNTEKT_RESULTAT, resultat.toMap())
        packet.putValue(
            Minsteinntekt.MINSTEINNTEKT_INNTEKTSPERIODER, checkNotNull(
                Minsteinntekt.jsonAdapterInntektPeriodeInfo.toJsonValue(createInntektPerioder(fakta))
            )
        )

        return packet
    }

    override fun onFailure(packet: Packet, error: Throwable?): Packet {
        packet.addProblem(
            Problem(
                type = URI("urn:dp:error:regel"),
                title = "Ukjent feil ved bruk av minsteinntektregel",
                instance = URI("urn:dp:regel:minsteinntekt")
            )
        )
        return packet
    }
}

object RapidHealthCheck : RapidsConnection.StatusListener, HealthCheck {
    var healthy: Boolean = false

    override fun onStartup(rapidsConnection: RapidsConnection) {
        healthy = true
    }

    override fun onReady(rapidsConnection: RapidsConnection) {
        healthy = true
    }

    override fun onNotReady(rapidsConnection: RapidsConnection) {
        healthy = false
    }

    override fun onShutdown(rapidsConnection: RapidsConnection) {
        healthy = false
    }

    override fun status(): HealthStatus = when (healthy) {
        true -> HealthStatus.UP
        false -> HealthStatus.DOWN
    }
}
