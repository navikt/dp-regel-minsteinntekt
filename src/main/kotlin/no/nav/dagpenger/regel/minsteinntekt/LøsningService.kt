package no.nav.dagpenger.regel.minsteinntekt

import de.huxhorn.sulky.ulid.ULID
import mu.KotlinLogging
import no.nav.dagpenger.inntekt.rpc.InntektHenter
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.BRUKT_INNTEKTSPERIODE
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.FANGST_OG_FISK
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.LÆRLING
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
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
            validate { it.demandAll("@behov", listOf(MINSTEINNTEKT)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id", BEREGNINGSDATO_NY_SRKIVEMÅTE) }
            validate { it.require(INNTEKT_ID) { inntektid -> ULID.parseULID(inntektid.asText()) } }
            validate { it.interestedIn(LÆRLING, FANGST_OG_FISK, AVTJENT_VERNEPLIKT, BRUKT_INNTEKTSPERIODE) }
        }.register(this)
    }

    companion object {
        const val MINSTEINNTEKT = "Minsteinntekt"
        const val INNTEKT_ID = "Inntekt"
        const val BEREGNINGSDATO_NY_SRKIVEMÅTE = "beregningsdato"
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val packetMedLøsning = løsFor(packet)
        context.send(packetMedLøsning.toJson())
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

        packet["@løsning"] = mapOf(MINSTEINNTEKT to mapOf(Minsteinntekt.MINSTEINNTEKT_NARE_EVALUERING to evaluering,
            Minsteinntekt.MINSTEINNTEKT_RESULTAT to resultat, Minsteinntekt.MINSTEINNTEKT_INNTEKTSPERIODER to createInntektPerioder(fakta)))
        return packet
    }
}
