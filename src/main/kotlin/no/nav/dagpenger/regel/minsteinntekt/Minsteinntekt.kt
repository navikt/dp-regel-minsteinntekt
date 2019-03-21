package no.nav.dagpenger.regel.minsteinntekt

import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.streamConfig
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import org.apache.kafka.streams.kstream.Predicate
import java.math.BigDecimal

class Minsteinntekt(val env: Environment) : River() {
    override val SERVICE_APP_ID: String = "dagpenger-regel-minsteinntekt"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT
    val ulidGenerator = ULID()

    val jsonAdapterInntekt = moshiInstance.adapter(Inntekt::class.java)
    val jsonAdapterInntektsPeriode = moshiInstance.adapter(InntektsPeriode::class.java)

    companion object {
        const val REGELIDENTIFIKATOR = "Minsteinntekt.v1"
        const val MINSTEINNTEKT_RESULTAT = "minsteinntektResultat"
        const val INNTEKT = "inntektV1"
        const val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        const val SENESTE_INNTEKTSMÅNED = "senesteInntektsmåned"
        const val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"
        const val FANGST_OG_FISK = "fangstOgFisk"
    }

    override fun getConfig() = streamConfig(
        appId = SERVICE_APP_ID,
        bootStapServerUrl = env.bootstrapServersUrl,
        credential = KafkaCredential(env.username, env.password)
    )

    override fun filterPredicates(): List<Predicate<String, Packet>> {
        return listOf(
            Predicate { _, packet -> !packet.hasField(MINSTEINNTEKT_RESULTAT) },
            Predicate { _, packet -> packet.hasField(INNTEKT) },
            Predicate { _, packet -> packet.hasField(SENESTE_INNTEKTSMÅNED) }
        )
    }

    override fun onPacket(packet: Packet): Packet {
        val inntekt: Inntekt =
            packet.getObjectValue(INNTEKT) { serialized -> checkNotNull(jsonAdapterInntekt.fromJson(serialized)) }
        val avtjentVernePlikt = packet.getNullableBoolean(AVTJENT_VERNEPLIKT) ?: false
        val senesteInntektsMåned = packet.getYearMonth(SENESTE_INNTEKTSMÅNED)
        val bruktInntektsPeriode =
            packet.getNullableObjectValue(BRUKT_INNTEKTSPERIODE) { jsonAdapterInntektsPeriode.fromJson(it) }
        val fangstOgFisk = packet.getNullableBoolean(FANGST_OG_FISK) ?: false

        val fakta = Fakta(inntekt, senesteInntektsMåned, bruktInntektsPeriode, avtjentVernePlikt, fangstOgFisk)

        val evaluering: Evaluering = inngangsVilkår.evaluer(fakta)

        val resultat = MinsteinntektSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            evaluering.resultat == Resultat.JA,
            createInntektPerioder(fakta)
        )
        packet.putValue(MINSTEINNTEKT_RESULTAT, resultat.toMap())
        return packet
    }

    fun createInntektPerioder(fakta: Fakta): List<InntektInfo> {
        val arbeidsInntekt = listOf(InntektKlasse.ARBEIDSINNTEKT)
        val medFangstOgFisk = listOf(InntektKlasse.ARBEIDSINNTEKT, InntektKlasse.FANGST_FISKE)

        return fakta.inntektsPerioder.toList().mapIndexed { index, list ->
            InntektInfo(
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

fun main(args: Array<String>) {
    val service = Minsteinntekt(Environment())
    service.start()
}

data class InntektInfo(
    val inntektsPeriode: InntektsPeriode,
    val inntekt: BigDecimal,
    val periode: Int,
    val inneholderFangstOgFisk: Boolean,
    val andel: BigDecimal
)