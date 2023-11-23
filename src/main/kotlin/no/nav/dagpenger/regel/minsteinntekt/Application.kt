package no.nav.dagpenger.regel.minsteinntekt

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import io.prometheus.client.CollectorRegistry
import no.nav.NarePrometheus
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.Problem
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.INNTEKT
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.MINSTEINNTEKT_RESULTAT
import no.nav.dagpenger.streams.KafkaAivenCredentials
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.streamConfigAiven
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import org.apache.kafka.streams.kstream.Predicate
import java.net.URI
import java.util.Properties

internal val narePrometheus = NarePrometheus(CollectorRegistry.defaultRegistry)
val config = Configuration()

fun main() {
    Application(config).start()
    // ApplicationBuilder(Config.config).start()
}

@Suppress("ktlint:standard:property-naming")
class Application(
    private val configuration: Configuration,
) : River(configuration.regelTopic) {
    override val SERVICE_APP_ID: String = configuration.application.id

    override val HTTP_PORT: Int = configuration.application.httpPort
    private val grunnbeløpStrategy = GrunnbeløpStrategy(configuration.unleash)

    companion object {
        const val BEREGNINGSDATO_GAMMEL_SKRIVEMÅTE = "beregningsDato"
    }

    override fun getConfig(): Properties {
        return streamConfigAiven(
            appId = SERVICE_APP_ID,
            bootStapServerUrl = configuration.kafka.aivenBrokers,
            aivenCredentials = KafkaAivenCredentials(),
        )
    }

    override fun filterPredicates(): List<Predicate<String, Packet>> {
        return listOf(
            Predicate { _, packet -> !packet.hasField(MINSTEINNTEKT_RESULTAT) },
            Predicate { _, packet -> packet.hasField(INNTEKT) },
            Predicate { _, packet -> packet.hasField(BEREGNINGSDATO_GAMMEL_SKRIVEMÅTE) },
        )
    }

    override fun onPacket(packet: Packet) = løsFor(packet)

    private fun løsFor(packet: Packet): Packet {
        val fakta = packetToFakta(packet, grunnbeløpStrategy)

        val evaluering: Evaluering =
            when {
                fakta.regelverksdato.erKoronaPeriode() -> {
                    narePrometheus.tellEvaluering { kravTilMinsteinntektKorona.evaluer(fakta) }
                }

                fakta.regelverksdato.erKoronaLærlingperiode() && fakta.lærling -> {
                    narePrometheus.tellEvaluering { kravTilMinsteinntektKorona.evaluer(fakta) }
                }

                else -> {
                    narePrometheus.tellEvaluering { kravTilMinsteinntekt.evaluer(fakta) }
                }
            }

        val resultat =
            MinsteinntektSubsumsjon(
                ulidGenerator.nextULID(),
                ulidGenerator.nextULID(),
                Minsteinntekt.REGELIDENTIFIKATOR,
                evaluering.resultat == Resultat.JA,
                evaluering.finnRegelBrukt(),
            )
        val jsonAdapterInntektPeriodeInfo: JsonAdapter<List<InntektPeriodeInfo>> =
            moshiInstance.adapter(Types.newParameterizedType(List::class.java, InntektPeriodeInfo::class.java))!!
        packet.putValue(MINSTEINNTEKT_RESULTAT, resultat.toMap())
        packet.putValue(
            Minsteinntekt.MINSTEINNTEKT_INNTEKTSPERIODER,
            checkNotNull(
                jsonAdapterInntektPeriodeInfo.toJsonValue(createInntektPerioder(fakta)),
            ),
        )

        return packet
    }

    override fun onFailure(
        packet: Packet,
        error: Throwable?,
    ): Packet {
        packet.addProblem(
            Problem(
                type = URI("urn:dp:error:regel"),
                title = "Ukjent feil ved bruk av minsteinntektregel",
                instance = URI("urn:dp:regel:minsteinntekt"),
            ),
        )
        return packet
    }
}
