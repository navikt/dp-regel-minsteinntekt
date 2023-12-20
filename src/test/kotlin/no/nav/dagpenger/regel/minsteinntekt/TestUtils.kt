package no.nav.dagpenger.regel.minsteinntekt

import io.getunleash.FakeUnleash
import no.nav.dagpenger.inntekt.v1.InntektKlasse
import no.nav.dagpenger.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.inntekt.v1.KlassifisertInntektMåned
import java.math.BigDecimal
import java.time.YearMonth

internal val grunnbeløpStrategy = GrunnbeløpStrategy(FakeUnleash().apply { this.disableAll() })

fun generateArbeidsinntekt(
    numberOfMonths: Int,
    beløpPerMnd: BigDecimal,
    senesteMåned: YearMonth =
        YearMonth.of(
            2019,
            1,
        ),
): List<KlassifisertInntektMåned> {
    return (0 until numberOfMonths).toList().map {
        KlassifisertInntektMåned(
            årMåned = senesteMåned.minusMonths(it.toLong()),
            klassifiserteInntekter =
            listOf(
                KlassifisertInntekt(beløpPerMnd, InntektKlasse.ARBEIDSINNTEKT),
            ),
        )
    }
}

fun generateFangstOgFiskInntekt(
    numberOfMonths: Int,
    beløpPerMnd: BigDecimal,
    senesteMåned: YearMonth =
        YearMonth.of(
            2019,
            1,
        ),
): List<KlassifisertInntektMåned> {
    return (0 until numberOfMonths).toList().map {
        KlassifisertInntektMåned(
            senesteMåned.minusMonths(it.toLong()),
            listOf(
                KlassifisertInntekt(
                    beløpPerMnd,
                    InntektKlasse.FANGST_FISKE,
                ),
            ),
        )
    }
}

fun generateArbeidsOgFangstOgFiskInntekt(
    numberOfMonths: Int,
    arbeidsInntektBeløpPerMnd: BigDecimal,
    fangstOgFiskeBeløpPerMnd: BigDecimal,
    senesteMåned: YearMonth =
        YearMonth.of(
            2019,
            1,
        ),
): List<KlassifisertInntektMåned> {
    return (0 until numberOfMonths).toList().map {
        KlassifisertInntektMåned(
            senesteMåned.minusMonths(it.toLong()),
            listOf(
                KlassifisertInntekt(arbeidsInntektBeløpPerMnd, InntektKlasse.ARBEIDSINNTEKT),
                KlassifisertInntekt(fangstOgFiskeBeløpPerMnd, InntektKlasse.FANGST_FISKE),
            ),
        )
    }
}

fun generate12MånederFangstOgFiskInntekt(): List<KlassifisertInntektMåned> {
    return generateFangstOgFiskInntekt(12, BigDecimal(50000))
}

fun generate36MånederFangstOgFiskInntekt(): List<KlassifisertInntektMåned> {
    return generateFangstOgFiskInntekt(36, BigDecimal(50000))
}
