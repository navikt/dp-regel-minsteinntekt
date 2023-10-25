package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.all
import no.nav.dagpenger.events.inntekt.v1.sumInntekt
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month

data class Fakta(
    val inntekt: Inntekt,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val verneplikt: Boolean,
    private val fangstOgFisk: Boolean,
    val beregningsdato: LocalDate,
    val regelverksdato: LocalDate,
    val lærling: Boolean = false,
    val grunnbeløp: BigDecimal,
) {

    internal fun erGyldigFangstOgFisk(): Boolean {
        val fangstOgFiskAvvikletFra = LocalDate.of(2022, 1, 1)
        return (fangstOgFisk && regelverksdato < fangstOgFiskAvvikletFra)
    }

    val inntektsPerioder = inntekt.splitIntoInntektsPerioder()

    val inntektsPerioderUtenBruktInntekt = if (bruktInntektsPeriode == null) {
        inntektsPerioder
    } else {
        inntekt.filterPeriod(
            bruktInntektsPeriode.førsteMåned,
            bruktInntektsPeriode.sisteMåned,
        ).splitIntoInntektsPerioder()
    }

    val arbeidsinntektSiste12: BigDecimal =
        inntektsPerioderUtenBruktInntekt.first.sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT))
    val arbeidsinntektSiste36: BigDecimal =
        inntektsPerioderUtenBruktInntekt.all().sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT))

    val inntektSiste12inkludertFangstOgFiske: BigDecimal = inntektsPerioderUtenBruktInntekt.first.sumInntekt(
        listOf(
            InntektKlasse.ARBEIDSINNTEKT,
            InntektKlasse.FANGST_FISKE,
        ),
    )
    val inntektSiste36inkludertFangstOgFiske: BigDecimal = inntektsPerioderUtenBruktInntekt.all()
        .sumInntekt(listOf(InntektKlasse.ARBEIDSINNTEKT, InntektKlasse.FANGST_FISKE))
}

fun LocalDate.erKoronaPeriode() = førsteKoronaperiode() || andreKoronaperiode() || tredjeKoronaperiode()

private fun LocalDate.førsteKoronaperiode() =
    (this in (LocalDate.of(2020, Month.MARCH, 20)..LocalDate.of(2020, Month.OCTOBER, 31)))

private fun LocalDate.andreKoronaperiode() =
    (this in (LocalDate.of(2021, Month.FEBRUARY, 19)..LocalDate.of(2021, Month.SEPTEMBER, 30)))

private fun LocalDate.tredjeKoronaperiode() =
    (this in (LocalDate.of(2021, Month.DECEMBER, 15)..LocalDate.of(2022, Month.MARCH, 31)))

fun LocalDate.førsteLærlingKoronaperiode() = this in (LocalDate.of(2020, Month.NOVEMBER, 1)..LocalDate.of(2021, Month.SEPTEMBER, 30))

fun LocalDate.andreLærlingKoronaperiode() = this.tredjeKoronaperiode()

fun LocalDate.erKoronaLærlingperiode() = this.førsteLærlingKoronaperiode() || this.andreLærlingKoronaperiode()
