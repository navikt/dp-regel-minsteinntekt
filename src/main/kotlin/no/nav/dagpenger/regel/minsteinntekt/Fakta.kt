package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import java.math.BigDecimal
import java.time.YearMonth

data class Fakta(
    val inntekt: Inntekt,
    val fraMåned: YearMonth,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val verneplikt: Boolean,
    val fangstOgFisk: Boolean,
    val grunnbeløp: BigDecimal = BigDecimal(96883)
) {
    val inntektsListe = bruktInntektsPeriode?.let {
        filterBruktInntekt(inntekt.inntektsListe, bruktInntektsPeriode)
    } ?: inntekt.inntektsListe

    val inntektSiste12 = sumArbeidsInntekt(inntektsListe, fraMåned, 11)
    val inntektSiste36 = sumArbeidsInntekt(inntektsListe, fraMåned, 35)

    val arbeidsInntektOgNæringsInntektSiste12 = sumArbeidsInntekt(inntektsListe, fraMåned, 11) + sumNæringsInntekt(inntektsListe, fraMåned, 11)
    val arbeidsInntektOgNæringsInntektSiste36 = sumArbeidsInntekt(inntektsListe, fraMåned, 35) + sumNæringsInntekt(inntektsListe, fraMåned, 35)
}