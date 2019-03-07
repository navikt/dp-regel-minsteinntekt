package no.nav.dagpenger.regel.minsteinntekt

import de.huxhorn.sulky.ulid.ULID
import mu.KotlinLogging
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.Service
import no.nav.dagpenger.streams.Topic
import no.nav.dagpenger.streams.Topics
import no.nav.dagpenger.streams.kbranch
import no.nav.dagpenger.streams.streamConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.json.JSONObject
import java.math.BigDecimal
import java.time.YearMonth
import java.util.Properties

private val LOGGER = KotlinLogging.logger {}

val dagpengerBehovTopic = Topic(
    Topics.DAGPENGER_BEHOV_EVENT.name,
    Serdes.StringSerde(),
    Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())
)

class MinsteinntektRegel(val env: Environment) : Service() {
    override val SERVICE_APP_ID: String = "dagpenger-regel-minsteinntekt"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT
    val ulidGenerator = ULID()
    val REGELIDENTIFIKATOR = "Minsteinntekt.v1"

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val service = MinsteinntektRegel(Environment())
            service.start()
        }
    }

    override fun setupStreams(): KafkaStreams {
        LOGGER.info { "Initiating start of $SERVICE_APP_ID" }
        return KafkaStreams(buildTopology(), getConfig())
    }

    internal fun buildTopology(): Topology {
        val builder = StreamsBuilder()

        val stream = builder.stream(
            dagpengerBehovTopic.name,
            Consumed.with(dagpengerBehovTopic.keySerde, dagpengerBehovTopic.valueSerde)
        )

        val (needsInntekt, needsSubsumsjon) = stream
            .peek { key, value -> LOGGER.info("Processing ${value.javaClass} with key $key") }
            .mapValues { value: JSONObject -> SubsumsjonsBehov(value) }
            .filter { _, behov -> shouldBeProcessed(behov) }
            .kbranch(
                { _, behov: SubsumsjonsBehov -> behov.needsHentInntektsTask() },
                { _, behov: SubsumsjonsBehov -> behov.needsMinsteinntektResultat() })

        needsInntekt.mapValues(this::addInntektTask)
        needsSubsumsjon.mapValues(this::addRegelresultat)

        needsInntekt.merge(needsSubsumsjon)
            .peek { key, value -> LOGGER.info("Producing ${value.javaClass} with key $key") }
            .mapValues { behov -> behov.jsonObject }
            .to(dagpengerBehovTopic.name, Produced.with(dagpengerBehovTopic.keySerde, dagpengerBehovTopic.valueSerde))

        return builder.build()
    }

    override fun getConfig(): Properties {
        val props = streamConfig(
            appId = SERVICE_APP_ID,
            bootStapServerUrl = env.bootstrapServersUrl,
            credential = KafkaCredential(env.username, env.password)
        )
        return props
    }

    private fun addInntektTask(behov: SubsumsjonsBehov): SubsumsjonsBehov {

        behov.addTask("hentInntekt")

        return behov
    }

    private fun addRegelresultat(behov: SubsumsjonsBehov): SubsumsjonsBehov {
        behov.addMinsteinntektResultat(
            MinsteinntektResultat(
                ulidGenerator.nextULID(),
                ulidGenerator.nextULID(),
                REGELIDENTIFIKATOR,
                oppfyllerKravTilMinsteinntekt(
                    behov.hasVerneplikt(),
                    behov.getInntekt(),
                    behov.getSenesteInntektsmåned(),
                    behov.getBruktInntektsPeriode(),
                    behov.hasFangstOgFisk())))
        return behov
    }
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
        .flatMap { it.klassifiserteInntekter
            .filter { it.inntektKlasse == InntektKlasse.ARBEIDSINNTEKT }
            .map { it.beløp } }.fold(BigDecimal.ZERO, BigDecimal::add)

    return sumGjeldendeMåneder
}

fun sumNæringsInntekt(inntektsListe: List<KlassifisertInntektMåned>, senesteMåned: YearMonth, lengde: Int): BigDecimal {
    val tidligsteMåned = finnTidligsteMåned(senesteMåned, lengde)

    val gjeldendeMåneder = inntektsListe.filter { it.årMåned <= senesteMåned && it.årMåned >= tidligsteMåned }

    val sumGjeldendeMåneder = gjeldendeMåneder
        .flatMap { it.klassifiserteInntekter
            .filter { it.inntektKlasse == InntektKlasse.NÆRINGSINNTEKT }
            .map { it.beløp } }.fold(BigDecimal.ZERO, BigDecimal::add)

    return sumGjeldendeMåneder
}

fun finnTidligsteMåned(senesteMåned: YearMonth, lengde: Int): YearMonth {

    return senesteMåned.minusMonths(lengde.toLong())
}

fun shouldBeProcessed(behov: SubsumsjonsBehov): Boolean {
    return when {
        behov.needsHentInntektsTask() -> true
        behov.needsMinsteinntektResultat() -> true
        else -> false
    }
}