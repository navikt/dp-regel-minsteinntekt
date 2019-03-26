package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.all
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import java.math.BigDecimal
import java.time.YearMonth

data class Fakta(
    val inntekt: Inntekt,
    val senesteInntektsMåned: YearMonth,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val verneplikt: Boolean,
    val fangstOgFisk: Boolean,
    val grunnbeløp: BigDecimal = BigDecimal(96883)
) {
    val inntektsPerioder = inntekt.splitIntoInntektsPerioder(senesteInntektsMåned)

    val inntektsPerioderUtenBruktInntekt = if (bruktInntektsPeriode == null) inntektsPerioder else inntekt.filterPeriod(
        bruktInntektsPeriode.førsteMåned,
        bruktInntektsPeriode.sisteMåned
    ).splitIntoInntektsPerioder(senesteInntektsMåned)

    val arbeidsinntektSiste12: BigDecimal = inntektsPerioderUtenBruktInntekt.first.sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT))
    val arbeidsinntektSiste36: BigDecimal = inntektsPerioderUtenBruktInntekt.all().sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT))

    val inntektSiste12inkludertFangstOgFiske: BigDecimal = inntektsPerioderUtenBruktInntekt.first.sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT, InntektKlasse.FANGST_FISKE))
    val inntektSiste36inkludertFangstOgFiske: BigDecimal = inntektsPerioderUtenBruktInntekt.all().sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT, InntektKlasse.FANGST_FISKE))
}