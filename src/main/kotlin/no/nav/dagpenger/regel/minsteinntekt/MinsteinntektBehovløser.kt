package no.nav.dagpenger.regel.minsteinntekt

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class MinsteinntektBehovløser(rapidsConnection: RapidsConnection) : River.PacketListener {

    companion object {
        const val REGELIDENTIFIKATOR = "Minsteinntekt.v1"
        const val INNTEKT = "inntektV1"
        const val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"
        const val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        const val FANGST_OG_FISKE = "oppfyllerKravTilFangstOgFisk"
        const val LÆRLING: String = "lærling"
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

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        TODO("Not yet implemented")
    }
}
