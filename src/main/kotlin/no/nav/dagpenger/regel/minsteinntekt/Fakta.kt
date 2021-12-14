package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.all
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import no.nav.dagpenger.regel.minsteinntekt.Application.Companion.unleash
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month

data class Fakta(
    val inntekt: Inntekt,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val verneplikt: Boolean,
    val fangstOgFisk: Boolean,
    val beregningsdato: LocalDate,
    val regelverksdato: LocalDate,
    val lærling: Boolean = false,
    val grunnbeløp: BigDecimal = when {
        isThisGjusteringTest(beregningsdato) -> Grunnbeløp.GjusteringsTest.verdi
        else -> getGrunnbeløpForRegel(Regel.Minsteinntekt).forDato(beregningsdato).verdi
    }
) {
    val inntektsPerioder = inntekt.splitIntoInntektsPerioder()

    val inntektsPerioderUtenBruktInntekt = if (bruktInntektsPeriode == null) inntektsPerioder else inntekt.filterPeriod(
        bruktInntektsPeriode.førsteMåned,
        bruktInntektsPeriode.sisteMåned
    ).splitIntoInntektsPerioder()

    val arbeidsinntektSiste12: BigDecimal =
        inntektsPerioderUtenBruktInntekt.first.sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT))
    val arbeidsinntektSiste36: BigDecimal =
        inntektsPerioderUtenBruktInntekt.all().sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT))

    val inntektSiste12inkludertFangstOgFiske: BigDecimal = inntektsPerioderUtenBruktInntekt.first.sumInntekt(
        listOf(
            InntektKlasse.ARBEIDSINNTEKT,
            InntektKlasse.FANGST_FISKE
        )
    )
    val inntektSiste36inkludertFangstOgFiske: BigDecimal = inntektsPerioderUtenBruktInntekt.all()
        .sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT, InntektKlasse.FANGST_FISKE))
}

internal fun isThisGjusteringTest(dato: LocalDate): Boolean {
    val gVirkning = LocalDate.of(2021, 5, 24)
    val isAfterGjustering = dato.isAfter(gVirkning.minusDays(1))
    return unleash.isEnabled(GJUSTERING_TEST) && isAfterGjustering
}
fun LocalDate.erKoronaPeriode() = førsteKoronaperiode() || andreKoronaperiode() || tredjeKoronaperiode()

private fun LocalDate.førsteKoronaperiode() =
    (this in (LocalDate.of(2020, Month.MARCH, 20)..LocalDate.of(2020, Month.OCTOBER, 31)))

private fun LocalDate.andreKoronaperiode() =
    (this in (LocalDate.of(2021, Month.FEBRUARY, 19)..LocalDate.of(2021, Month.SEPTEMBER, 30)))

// TODO: Sette korrekt fradato, når det er klart.
private fun LocalDate.tredjeKoronaperiode() =
    (this in (LocalDate.of(2021, Month.DECEMBER, 14)..LocalDate.of(2022, Month.FEBRUARY, 28)))

fun LocalDate.erKoronaLærlingPeriode() = this in (LocalDate.of(2020, Month.NOVEMBER, 1)..LocalDate.of(2021, Month.SEPTEMBER, 30))
