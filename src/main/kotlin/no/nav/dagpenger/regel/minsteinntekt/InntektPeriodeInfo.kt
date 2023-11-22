package no.nav.dagpenger.regel.minsteinntekt

import java.math.BigDecimal

data class InntektPeriodeInfo(
    val inntektsPeriode: InntektsPeriode,
    val inntekt: BigDecimal,
    val periode: Int,
    val inneholderFangstOgFisk: Boolean,
    val andel: BigDecimal,
) {

    fun toMap(): Map<String, Any> {
        return mapOf(
            "inntektsPeriode" to inntektsPeriode.toMap(),
            "inntekt" to inntekt,
            "periode" to periode,
            "inneholderFangstOgFisk" to inneholderFangstOgFisk,
            "andel" to andel,
        )
    }

    companion object {
        fun List<InntektPeriodeInfo>.toMaps() = this.map { it.toMap() }
    }
}
