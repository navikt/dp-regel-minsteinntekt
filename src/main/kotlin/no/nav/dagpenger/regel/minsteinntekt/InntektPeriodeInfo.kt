package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.inntekt.v1.InntektKlasse
import no.nav.dagpenger.inntekt.v1.sumInntekt
import java.math.BigDecimal

data class InntektPeriodeInfo(
    val inntektsPeriode: InntektsPeriode,
    val inntekt: BigDecimal,
    val periode: Int,
    val inneholderFangstOgFisk: Boolean,
    val andel: BigDecimal,
) {
    fun toMap(): Map<String, Any> =
        mapOf(
            "inntektsPeriode" to inntektsPeriode.toMap(),
            "inntekt" to inntekt,
            "periode" to periode,
            "inneholderFangstOgFisk" to inneholderFangstOgFisk,
            "andel" to andel,
        )

    companion object {
        fun List<InntektPeriodeInfo>.toMaps() = this.map { it.toMap() }
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
            fakta.inntektsPerioderUtenBruktInntekt.toList()[index].any {
                it.klassifiserteInntekter.any { it.inntektKlasse == InntektKlasse.FANGST_FISKE }
            },
            fakta.inntektsPerioderUtenBruktInntekt.toList()[index].sumInntekt(
                if (fakta.erGyldigFangstOgFisk()) medFangstOgFisk else arbeidsInntekt,
            ),
        )
    }
}
