package no.nav.dagpenger.regel.minsteinntekt

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.nare.core.evaluations.Evaluering

class Minsteinntekt {
    companion object {
        const val REGELIDENTIFIKATOR = "Minsteinntekt.v1"
        const val MINSTEINNTEKT_RESULTAT = "minsteinntektResultat"
        const val MINSTEINNTEKT_INNTEKTSPERIODER = "minsteinntektInntektsPerioder"
        const val INNTEKT = "inntektV1"
        const val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        const val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"
        const val FANGST_OG_FISK = "oppfyllerKravTilFangstOgFisk"
        const val LÆRLING: String = "lærling"
        const val REGELVERKSDATO = "regelverksdato"

        val jsonAdapterInntektPeriodeInfo: JsonAdapter<List<InntektPeriodeInfo>> =
            moshiInstance.adapter(Types.newParameterizedType(List::class.java, InntektPeriodeInfo::class.java))!!

        val jsonAdapterEvaluering: JsonAdapter<Evaluering> = moshiInstance.adapter(Evaluering::class.java)
    }
}

fun createInntektPerioder(fakta: Fakta): List<InntektPeriodeInfo> {
    val arbeidsInntekt = listOf(InntektKlasse.ARBEIDSINNTEKT)
    val medFangstOgFisk = listOf(InntektKlasse.ARBEIDSINNTEKT, InntektKlasse.FANGST_FISKE)

    return fakta.inntektsPerioder.toList().mapIndexed { index, list ->
        InntektPeriodeInfo(
            InntektsPeriode(
                list.first().årMåned,
                list.last().årMåned,
            ),
            list.sumInntekt(if (fakta.erGyldigFangstOgFisk()) medFangstOgFisk else arbeidsInntekt),
            index + 1,
            fakta.inntektsPerioderUtenBruktInntekt.toList()[index].any { it.klassifiserteInntekter.any { it.inntektKlasse == InntektKlasse.FANGST_FISKE } },
            fakta.inntektsPerioderUtenBruktInntekt.toList()[index].sumInntekt(if (fakta.erGyldigFangstOgFisk()) medFangstOgFisk else arbeidsInntekt),
        )
    }
}
