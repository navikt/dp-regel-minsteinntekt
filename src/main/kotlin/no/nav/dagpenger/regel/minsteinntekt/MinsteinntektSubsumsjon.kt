package no.nav.dagpenger.regel.minsteinntekt

import java.time.YearMonth

data class MinsteinntektSubsumsjon(
    val sporingsId: String,
    val subsumsjonsId: String,
    val regelidentifikator: String,
    val oppfyllerMinsteinntekt: Boolean,
    val beregningsregel: Beregningsregel
) {

    companion object {
        const val SPORINGSID = "sporingsId"
        const val SUBSUMSJONSID = "subsumsjonsId"
        const val REGELIDENTIFIKATOR = "regelIdentifikator"
        const val OPPFYLLER_MINSTEINNTEKT = "oppfyllerMinsteinntekt"
        const val BEREGNINGSREGEL = "beregningsregel"
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            SPORINGSID to sporingsId,
            SUBSUMSJONSID to subsumsjonsId,
            REGELIDENTIFIKATOR to regelidentifikator,
            OPPFYLLER_MINSTEINNTEKT to oppfyllerMinsteinntekt,
            BEREGNINGSREGEL to beregningsregel
        )
    }
}

data class InntektsPeriode(
    val førsteMåned: YearMonth,
    val sisteMåned: YearMonth
)

enum class Beregningsregel {
    ORDINAER,
    KORONA
}
