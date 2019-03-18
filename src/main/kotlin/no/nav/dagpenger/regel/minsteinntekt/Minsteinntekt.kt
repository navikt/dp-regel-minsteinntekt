package no.nav.dagpenger.regel.minsteinntekt

import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.streamConfig
import org.apache.kafka.streams.kstream.Predicate
import java.math.BigDecimal
import java.time.YearMonth

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
        val resultat = MinsteinntektSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            oppfyllerKravTilMinsteinntekt(
                packet.getBoolean(AVTJENT_VERNEPLIKT),
                packet.getObjectValue(INNTEKT) { serialized -> checkNotNull(jsonAdapterInntekt.fromJson(serialized)) },
                packet.getYearMonth(SENESTE_INNTEKTSMÅNED),
                packet.getObjectValue(BRUKT_INNTEKTSPERIODE) { serialized -> checkNotNull(jsonAdapterInntektsPeriode.fromJson(serialized)) },
                packet.getBoolean(FANGST_OG_FISK)
            )
        )

        packet.putValue(MINSTEINNTEKT_RESULTAT, resultat.toMap())
        return packet
    }
}

fun main(args: Array<String>) {
    val service = Minsteinntekt(Environment())
    service.start()
}

fun oppfyllerKravTilMinsteinntekt(
    verneplikt: Boolean,
    inntekt: Inntekt,
    fraMåned: YearMonth,
    bruktInntektsPeriode: InntektsPeriode? = null,
    fangstOgFisk: Boolean
): Boolean {

    val inntektsListe = bruktInntektsPeriode?.let {
        filterBruktInntekt(inntekt.inntektsListe, bruktInntektsPeriode)
    } ?: inntekt.inntektsListe

    val enG = BigDecimal(96883)

    var inntektSiste12 = sumArbeidsInntekt(inntektsListe, fraMåned, 11)
    var inntektSiste36 = sumArbeidsInntekt(inntektsListe, fraMåned, 35)

    if (fangstOgFisk) {
        inntektSiste12 += sumNæringsInntekt(inntektsListe, fraMåned, 11)
        inntektSiste36 += sumNæringsInntekt(inntektsListe, fraMåned, 35)
    }

    if (inntektSiste12 > (enG.times(BigDecimal(1.5))) || inntektSiste36 > (enG.times(BigDecimal(3)))) {
        return true
    }

    return verneplikt
}

fun filterBruktInntekt(
    inntektsListe: List<KlassifisertInntektMåned>,
    bruktInntektsPeriode: InntektsPeriode
): List<KlassifisertInntektMåned> {

    return inntektsListe.filter {
        it.årMåned.isBefore(bruktInntektsPeriode.førsteMåned) || it.årMåned.isAfter(bruktInntektsPeriode.sisteMåned)
    }
}

fun sumArbeidsInntekt(inntektsListe: List<KlassifisertInntektMåned>, senesteMåned: YearMonth, lengde: Int): BigDecimal {
    val tidligsteMåned = finnTidligsteMåned(senesteMåned, lengde)

    val gjeldendeMåneder = inntektsListe.filter { it.årMåned <= senesteMåned && it.årMåned >= tidligsteMåned }

    val sumGjeldendeMåneder = gjeldendeMåneder
        .flatMap {
            it.klassifiserteInntekter
                .filter { it.inntektKlasse == InntektKlasse.ARBEIDSINNTEKT }
                .map { it.beløp }
        }.fold(BigDecimal.ZERO, BigDecimal::add)

    return sumGjeldendeMåneder
}

fun sumNæringsInntekt(inntektsListe: List<KlassifisertInntektMåned>, senesteMåned: YearMonth, lengde: Int): BigDecimal {
    val tidligsteMåned = finnTidligsteMåned(senesteMåned, lengde)

    val gjeldendeMåneder = inntektsListe.filter { it.årMåned <= senesteMåned && it.årMåned >= tidligsteMåned }

    val sumGjeldendeMåneder = gjeldendeMåneder
        .flatMap {
            it.klassifiserteInntekter
                .filter { it.inntektKlasse == InntektKlasse.FANGST_FISKE }
                .map { it.beløp }
        }.fold(BigDecimal.ZERO, BigDecimal::add)

    return sumGjeldendeMåneder
}

fun finnTidligsteMåned(senesteMåned: YearMonth, lengde: Int): YearMonth {

    return senesteMåned.minusMonths(lengde.toLong())
}
