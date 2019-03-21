package no.nav.dagpenger.regel.minsteinntekt

import java.time.YearMonth

data class MinsteinntektSubsumsjon(val sporingsId: String, val subsumsjonsId: String, val regelidentifikator: String, val oppfyllerMinsteinntekt: Boolean, val inntektsPerioder: List<InntektInfo>) {

    companion object {
        val SPORINGSID = "sporingsId"
        val SUBSUMSJONSID = "subsumsjonsId"
        val REGELIDENTIFIKATOR = "regelIdentifikator"
        val OPPFYLLER_MINSTEINNTEKT = "oppfyllerMinsteinntekt"
        val INNTEKTSPERIODER = "inntektsPerioder"
    }
    fun toMap(): Map<String, Any> {
        return mapOf(
            SPORINGSID to sporingsId,
            SUBSUMSJONSID to subsumsjonsId,
            REGELIDENTIFIKATOR to regelidentifikator,
            OPPFYLLER_MINSTEINNTEKT to oppfyllerMinsteinntekt,
            INNTEKTSPERIODER to inntektsPerioder
        )
    }
}

data class InntektsPeriode(
    val førsteMåned: YearMonth,
    val sisteMåned: YearMonth
)
