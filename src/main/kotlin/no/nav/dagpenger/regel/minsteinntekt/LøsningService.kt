package no.nav.dagpenger.regel.minsteinntekt

import com.fasterxml.jackson.databind.JsonNode
import de.huxhorn.sulky.ulid.ULID
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.inntekt.rpc.InntektHenter
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asYearMonth
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat

class LøsningService(
    rapidsConnection: RapidsConnection,
    private val inntektHenter: InntektHenter
) : River.PacketListener {
    private val log = KotlinLogging.logger {}
    private val sikkerlogg = KotlinLogging.logger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf("Minsteinntekt")) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id", "vedtakId") }
            validate { it.require("inntektId") { id -> id.asULID() } }
            validate { it.requireKey("beregningsdato") }
            validate { it.interestedIn("lærling", "oppfyllerKravTilFangstOgFisk", "harAvtjentVerneplikt", "bruktInntektsPeriode") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        withLoggingContext(
            "behovId" to packet["@id"].asText(),
            "vedtakId" to packet["vedtakId"].asText()
        ) {
            val packetMedLøsning = løsFor(packet)
            context.send(packetMedLøsning.toJson())
            log.info { "løser behov for ${packet["@id"].asText()}" }
        }
    }

    override fun onError(problems: MessageProblems, context: RapidsConnection.MessageContext) {
        log.info { problems.toString() }
        sikkerlogg.info { problems.toExtendedReport() }
    }

    private fun løsFor(packet: JsonMessage): JsonMessage {
        val fakta = packet.toFakta(inntektHenter)
        val evaluering: Evaluering = if (fakta.beregningsdato.erKoronaPeriode()) {
            kravTilMinsteinntektKorona.evaluer(fakta)
        } else {
            kravTilMinsteinntekt.evaluer(fakta)
        }
        val resultat = MinsteinntektSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            Minsteinntekt.REGELIDENTIFIKATOR,
            evaluering.resultat == Resultat.JA,
            evaluering.finnRegelBrukt()
        )

        packet["@løsning"] = mapOf("Minsteinntekt" to mapOf("resultat" to resultat, "inntektsperioder" to createInntektPerioder(fakta)))
        return packet
    }
}

fun JsonNode.asULID(): ULID.Value = asText().let { ULID.parseULID(it) }

internal fun JsonMessage.toFakta(inntektHenter: InntektHenter): Fakta {
    val inntekt = this["inntektId"].asULID().let { runBlocking { inntektHenter.hentKlassifisertInntekt(it.toString()) } }
    val avtjentVerneplikt = this["harAvtjentVerneplikt"].asBoolean(false)
    val bruktInntektsPeriode =
        this["bruktInntektsPeriode"].takeIf(JsonNode::isObject)
            ?.let { InntektsPeriode(it["førsteMåned"].asYearMonth(), it["sisteMåned"].asYearMonth()) }
    val fangstOgFisk = this["oppfyllerKravTilFangstOgFisk"].asBoolean(false)
    val beregningsDato = this["beregningsdato"].asLocalDate()
    val lærling = this["lærling"].asBoolean(false)

    return Fakta(
        inntekt = inntekt,
        bruktInntektsPeriode = bruktInntektsPeriode,
        verneplikt = avtjentVerneplikt,
        fangstOgFisk = fangstOgFisk,
        beregningsdato = beregningsDato,
        lærling = lærling
    )
}
