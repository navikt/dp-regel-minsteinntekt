package no.nav.dagpenger.regel.minsteinntekt

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat

class MinsteinntektBehovløser(rapidsConnection: RapidsConnection) : River.PacketListener {
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

        internal val rapidFilter: River.() -> Unit = {
            validate {
                it.requireKey(
                    INNTEKT,
                    BEREGNINGSDATO,
                )
            }
            validate {
                it.interestedIn(
                    AVTJENT_VERNEPLIKT,
                    FANGST_OG_FISKE,
                    LÆRLING,
                    REGELVERKSDATO,
                    BRUKT_INNTEKTSPERIODE,
                )
            }
            validate { it.rejectKey(MINSTEINNTEKT_RESULTAT) }
        }
    }

    init {
        River(rapidsConnection).apply(rapidFilter).register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
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
                Minsteinntekt.REGELIDENTIFIKATOR,
                evaluering.resultat == Resultat.JA,
                evaluering.finnRegelBrukt(),
            )

        packet[MINSTEINNTEKT_RESULTAT] = resultat.toMap()
        // TODO: Bytt ut moshi
        packet[MINSTEINNTEKT_INNTEKTSPERIODER] =
            checkNotNull(
                Minsteinntekt.jsonAdapterInntektPeriodeInfo.toJsonValue(createInntektPerioder(fakta)),
            )

        context.publish(packet.toJson())
    }
}
