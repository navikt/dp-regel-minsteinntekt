package no.nav.dagpenger.regel.minsteinntekt

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import de.huxhorn.sulky.ulid.ULID
import io.prometheus.client.CollectorRegistry
import java.math.BigDecimal
import java.net.URI
import no.nav.NarePrometheus
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.Problem
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.streamConfig
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import org.apache.kafka.streams.kstream.Predicate

internal val narePrometheus = NarePrometheus(CollectorRegistry.defaultRegistry)

class Minsteinntekt(private val configuration: Configuration) : River(configuration.behovTopic) {
    override val SERVICE_APP_ID: String = configuration.application.id
    override val HTTP_PORT: Int = configuration.application.httpPort

    val ulidGenerator = ULID()

    val jsonAdapterInntektPeriodeInfo: JsonAdapter<List<InntektPeriodeInfo>> =
        moshiInstance.adapter(Types.newParameterizedType(List::class.java, InntektPeriodeInfo::class.java))!!

    val jsonAdapterEvaluering: JsonAdapter<Evaluering> = moshiInstance.adapter(Evaluering::class.java)

    companion object {
        const val REGELIDENTIFIKATOR = "Minsteinntekt.v1"
        const val MINSTEINNTEKT_RESULTAT = "minsteinntektResultat"
        const val MINSTEINNTEKT_INNTEKTSPERIODER = "minsteinntektInntektsPerioder"
        const val MINSTEINNTEKT_NARE_EVALUERING = "minsteinntektNareEvaluering"
        const val INNTEKT = "inntektV1"
        const val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        const val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"
        const val FANGST_OG_FISK = "oppfyllerKravTilFangstOgFisk"
        const val BEREGNINGSDAGTO = "beregningsDato"
        const val LÆRLING: String = "lærling"
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
            Predicate { _, packet -> packet.hasField(BEREGNINGSDAGTO) }
        )
    }

    override fun onPacket(packet: Packet): Packet {
        val fakta = packetToFakta(packet)

        val evaluering: Evaluering = if (fakta.beregningsdato.erKoronaPeriode()) {
            narePrometheus.tellEvaluering { kravTilMinsteinntektKorona.evaluer(fakta) }
        } else {
            narePrometheus.tellEvaluering { kravTilMinsteinntekt.evaluer(fakta) }
        }

        val resultat = MinsteinntektSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            evaluering.resultat == Resultat.JA,
            evaluering.finnRegelBrukt()
        )

        packet.putValue(MINSTEINNTEKT_NARE_EVALUERING, jsonAdapterEvaluering.toJson(evaluering))
        packet.putValue(MINSTEINNTEKT_RESULTAT, resultat.toMap())
        packet.putValue(
            MINSTEINNTEKT_INNTEKTSPERIODER, checkNotNull(
                jsonAdapterInntektPeriodeInfo.toJsonValue(createInntektPerioder(fakta))
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

    fun createInntektPerioder(fakta: Fakta): List<InntektPeriodeInfo> {
        val arbeidsInntekt = listOf(InntektKlasse.ARBEIDSINNTEKT)
        val medFangstOgFisk = listOf(InntektKlasse.ARBEIDSINNTEKT, InntektKlasse.FANGST_FISKE)

        return fakta.inntektsPerioder.toList().mapIndexed { index, list ->
            InntektPeriodeInfo(
                InntektsPeriode(
                    list.first().årMåned,
                    list.last().årMåned
                ),
                list.sumInntekt(if (fakta.fangstOgFisk) medFangstOgFisk else arbeidsInntekt),
                index + 1,
                fakta.inntektsPerioderUtenBruktInntekt.toList()[index].any { it.klassifiserteInntekter.any { it.inntektKlasse == InntektKlasse.FANGST_FISKE } },
                fakta.inntektsPerioderUtenBruktInntekt.toList()[index].sumInntekt(if (fakta.fangstOgFisk) medFangstOgFisk else arbeidsInntekt)
            )
        }
    }
}

val configuration = Configuration()
fun main() {
    val service = Minsteinntekt(configuration)
    service.start()
}

data class InntektPeriodeInfo(
    val inntektsPeriode: InntektsPeriode,
    val inntekt: BigDecimal,
    val periode: Int,
    val inneholderFangstOgFisk: Boolean,
    val andel: BigDecimal
)
