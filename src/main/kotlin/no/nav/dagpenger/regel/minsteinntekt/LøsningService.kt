package no.nav.dagpenger.regel.minsteinntekt

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat

class LøsningService(
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.requireAll("@behov", listOf(MINSTEINNTEKT)) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("@id", INNTEKT, BEREGNINGSDATO) }
            validate { it.interestedIn(LÆRLING, FANGST_OG_FISK, AVTJENT_VERNEPLIKT, BRUKT_INNTEKTSPERIODE) }
        }.register(this)
    }

    companion object {
        const val MINSTEINNTEKT = "Minsteinntekt"
        const val INNTEKT = "inntektV1"
        const val BEREGNINGSDATO = "beregningsDato"
        const val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        const val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"
        const val FANGST_OG_FISK = "oppfyllerKravTilFangstOgFisk"
        const val LÆRLING: String = "lærling"
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val fakta = packet.toFakta()

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

        packet.putValue(Minsteinntekt.MINSTEINNTEKT_NARE_EVALUERING, jsonAdapterEvaluering.toJson(evaluering))
        packet.putValue(Minsteinntekt.MINSTEINNTEKT_RESULTAT, resultat.toMap())
        packet.putValue(
            Minsteinntekt.MINSTEINNTEKT_INNTEKTSPERIODER, checkNotNull(
                jsonAdapterInntektPeriodeInfo.toJsonValue(createInntektPerioder(fakta))
            )
        )

        return packet
    }
}
