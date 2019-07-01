package no.nav.dagpenger.regel.minsteinntekt

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import de.huxhorn.sulky.ulid.ULID
import io.prometheus.client.CollectorRegistry
import no.nav.NarePrometheus
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.Problem
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.plain.RiverConsumer
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import java.math.BigDecimal
import java.net.URI
import java.util.Properties
import java.util.function.Predicate

private val narePrometheus = NarePrometheus(CollectorRegistry.defaultRegistry)

class Minsteinntekt(env: Environment) : RiverConsumer(env.bootstrapServersUrl) {
    override val SERVICE_APP_ID: String = "dagpenger-regel-minsteinntekt"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT

    private val ulidGenerator = ULID()

    val jsonAdapterInntektPeriodeInfo: JsonAdapter<List<InntektPeriodeInfo>> =
        moshiInstance.adapter(Types.newParameterizedType(List::class.java, InntektPeriodeInfo::class.java))!!

    companion object {
        const val REGELIDENTIFIKATOR = "Minsteinntekt.v1"
        const val MINSTEINNTEKT_RESULTAT = "minsteinntektResultat"
        const val MINSTEINNTEKT_INNTEKTSPERIODER = "minsteinntektInntektsPerioder"
        const val INNTEKT = "inntektV1"
        const val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        const val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"
        const val FANGST_OG_FISK = "oppfyllerKravTilFangstOgFisk"
        const val BEREGNINGSDAGTO = "beregningsDato"
    }

    private val kafkaCredential = KafkaCredential(env.username, env.password)

    override fun getConsumerConfig(credential: KafkaCredential?): Properties {
        return super.getConsumerConfig(kafkaCredential)
    }

    override fun getProducerConfig(credential: KafkaCredential?): Properties {
        return super.getProducerConfig(kafkaCredential)
    }

    override fun filterPredicates(): List<Predicate<Packet>> {
        return listOf(
            Predicate { packet -> !packet.hasField(MINSTEINNTEKT_RESULTAT) },
            Predicate { packet -> packet.hasField(INNTEKT) },
            Predicate { packet -> packet.hasField(BEREGNINGSDAGTO) }
        )
    }

    override fun onPacket(packet: Packet): Packet {
        val fakta = packetToFakta(packet)

        val evaluering: Evaluering = narePrometheus.tellEvaluering { inngangsVilkår.evaluer(fakta) }
        val resultat = MinsteinntektSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            evaluering.resultat == Resultat.JA
        )

        packet.putValue(MINSTEINNTEKT_RESULTAT, resultat.toMap())
        packet.putValue(MINSTEINNTEKT_INNTEKTSPERIODER, checkNotNull(
            jsonAdapterInntektPeriodeInfo.toJsonValue(createInntektPerioder(fakta))
        ))

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

fun main() {
    Minsteinntekt(Environment()).apply {
        Runtime.getRuntime().addShutdownHook(Thread {
            this.stop()
        })
        this.start()
    }
}

data class InntektPeriodeInfo(
    val inntektsPeriode: InntektsPeriode,
    val inntekt: BigDecimal,
    val periode: Int,
    val inneholderFangstOgFisk: Boolean,
    val andel: BigDecimal
)