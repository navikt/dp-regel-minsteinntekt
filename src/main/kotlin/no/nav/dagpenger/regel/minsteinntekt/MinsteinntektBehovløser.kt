package no.nav.dagpenger.regel.minsteinntekt

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import io.prometheus.client.CollectorRegistry
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.NarePrometheus
import no.nav.dagpenger.regel.minsteinntekt.InntektPeriodeInfo.Companion.toMaps
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import java.net.URI

private val sikkerLogg = KotlinLogging.logger("tjenestekall")

class MinsteinntektBehovløser(
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    companion object {
        const val REGELIDENTIFIKATOR = "Minsteinntekt.v1"
        const val INNTEKT = "inntektV1"
        const val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"
        const val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        const val FANGST_OG_FISKE = "oppfyllerKravTilFangstOgFisk"
        const val LÆRLING = "lærling"
        const val BEREGNINGSDATO = "beregningsDato"
        const val REGELVERKSDATO = "regelverksdato"
        const val MINSTEINNTEKT_RESULTAT = "minsteinntektResultat"
        const val MINSTEINNTEKT_INNTEKTSPERIODER = "minsteinntektInntektsPerioder"
        const val BEHOV_ID = "behovId"
        const val PROBLEM = "system_problem"

        internal val narePrometheus = NarePrometheus(CollectorRegistry.defaultRegistry)
        internal val rapidFilter: River.() -> Unit = {
            validate { it.requireKey(BEHOV_ID) }
            validate { it.requireKey(INNTEKT, BEREGNINGSDATO) }
            validate {
                it.interestedIn(
                    AVTJENT_VERNEPLIKT,
                    FANGST_OG_FISKE,
                    LÆRLING,
                    REGELVERKSDATO,
                    BRUKT_INNTEKTSPERIODE,
                )
            }
            validate { it.forbid(MINSTEINNTEKT_RESULTAT) }
            validate { it.forbid(PROBLEM) }
        }
    }

    init {
        River(rapidsConnection).apply(rapidFilter).register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        withLoggingContext("behovId" to packet[BEHOV_ID].asText()) {
            try {
                sikkerLogg.info("Mottok behov for vurdering av minsteinntekt: ${packet.toJson()}")

                val fakta = packetToFakta(packet, GrunnbeløpStrategy(Config.unleash))
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
                        REGELIDENTIFIKATOR,
                        evaluering.resultat == Resultat.JA,
                        evaluering.finnRegelBrukt(),
                    )

                packet[MINSTEINNTEKT_RESULTAT] = resultat.toMap()
                packet[MINSTEINNTEKT_INNTEKTSPERIODER] = createInntektPerioder(fakta).toMaps()

                context.publish(packet.toJson())
                sikkerLogg.info { "Løste behov for vurdering av minsteinntekt $resultat med fakta $fakta" }
            } catch (e: Exception) {
                val problem =
                    Problem(
                        type = URI("urn:dp:error:regel"),
                        title = "Ukjent feil ved bruk av minsteinntektregel",
                        instance = URI("urn:dp:regel:minsteinntekt"),
                    )
                packet[PROBLEM] = problem.toMap
                context.publish(packet.toJson())
                throw e
            }
        }
    }
}
