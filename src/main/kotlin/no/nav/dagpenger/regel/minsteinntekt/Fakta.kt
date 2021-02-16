package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.all
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month

data class Fakta(
    val inntekt: Inntekt,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val verneplikt: Boolean,
    val fangstOgFisk: Boolean,
    val beregningsdato: LocalDate,
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

internal fun isThisGjusteringTest(
    beregningsdato: LocalDate
): Boolean {
    val isBeregningsDatoAfterGjustering = beregningsdato.isAfter(LocalDate.of(2020, Month.SEPTEMBER, 1).minusDays(1))
    return config.features.gjustering() && isBeregningsDatoAfterGjustering
}
// NB! tilfeldig valgte datoer for den andre perioden!
fun LocalDate.erKoronaPeriode() = førsteKoronaperiode() || andreKoronaperiode()

private fun LocalDate.førsteKoronaperiode() =
    (this in (LocalDate.of(2020, Month.MARCH, 20)..LocalDate.of(2020, Month.OCTOBER, 31)))

private fun LocalDate.andreKoronaperiode() =
    (this in (LocalDate.of(2021, 2, 1)..LocalDate.of(2021, Month.SEPTEMBER, 30)) && config.features.koronaperiode2())

fun LocalDate.erKoronaLærlingPeriode() = this in (LocalDate.of(2020, Month.NOVEMBER, 1)..LocalDate.of(2021, Month.SEPTEMBER, 30))
