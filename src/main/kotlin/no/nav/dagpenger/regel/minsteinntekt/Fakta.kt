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
    val isBeregningsDatoAfterGjustering = beregningsdato.isAfter(LocalDate.of(2020, 9, 1).minusDays(1))
    return config.features.gjustering() && isBeregningsDatoAfterGjustering
}

fun LocalDate.erKoronaPeriode() = this in (LocalDate.of(2020, 3, 20)..LocalDate.of(2020, 10, 31))
//NB! tilfeldig valgte datoer
fun LocalDate.erKoronaPeriode2() = this in (LocalDate.of(2021, 2, 1)..LocalDate.of(2021, 6, 30))
fun LocalDate.erKoronaLærlingPeriode() = this in (LocalDate.of(2020, 11, 1)..LocalDate.of(2021, 3, 31))
