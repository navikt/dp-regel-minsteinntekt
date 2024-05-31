package no.nav.dagpenger.regel.minsteinntekt.inngangsvilkårOrdinær

import no.nav.dagpenger.inntekt.v1.Inntekt
import no.nav.dagpenger.inntekt.v1.InntektKlasse
import no.nav.dagpenger.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Fakta
import no.nav.dagpenger.regel.minsteinntekt.grunnbeløpStrategy
import no.nav.dagpenger.regel.minsteinntekt.ordinærSiste12Måneder
import no.nav.dagpenger.regel.minsteinntekt.ordinærSiste36Måneder
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

internal class InngangsvilkårOrdinærTest {
    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du ikke har inntekt siste 12 mnd`() {
        val beregningsdato = LocalDate.of(2019, 5, 10)
        val fakta =
            Fakta(
                inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = true,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste12Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har tjent litt for lite siste 12 mnd`() {
        val inntekt = generateArbeidsInntekt(1..3, BigDecimal(1))

        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = true,
                fangstOgFiske = false,
                beregningsdato = LocalDate.of(2019, 5, 10),
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = BigDecimal(4),
            )

        val evaluering = ordinærSiste12Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal gi rett til dagpenger i følge § 4-4 dersom du har hatt nok inntekt siste 12 mnd`() {
        val inntekt = generate12MånederArbeidsInntekt()

        val beregningsdato = LocalDate.of(2019, 5, 10)
        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = true,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste12Måneder.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du ikke har inntekt siste 36 mnd`() {
        val beregningsdato = LocalDate.of(2019, 5, 10)
        val fakta =
            Fakta(
                inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = true,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste36Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har tjent litt for lite siste 36 mnd`() {
        val inntekt = generateArbeidsInntekt(1..24, BigDecimal(1))

        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = true,
                fangstOgFiske = false,
                beregningsdato = LocalDate.of(2019, 5, 10),
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = BigDecimal(23),
            )

        val evaluering = ordinærSiste36Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal gi rett til dagpenger i følge § 4-4 dersom du har hatt nok inntekt siste 36 mnd`() {
        val inntekt = generate36MånederArbeidsInntekt()

        val beregningsdato = LocalDate.of(2019, 5, 10)
        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = true,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste36Måneder.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom man har næringsinntekt siste 12 mnd, men er fangst og fisk er ikke oppfylt `() {
        val inntekt = generateFangstOgFiske(1..12, BigDecimal(50000))

        val beregningsdato = LocalDate.of(2019, 5, 10)
        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = true,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste12Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom man har næringsinntekt siste 36 mnd, men er fangst og fisk er ikke oppfylt `() {
        val inntekt = generateFangstOgFiske(1..36, BigDecimal(50000))

        val beregningsdato = LocalDate.of(2019, 5, 10)
        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = true,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste36Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du ikke hatt nok inntekt siste 12 mnd, på grunn av minus-inntekt`() {
        val inntekt =
            listOf(
                KlassifisertInntektMåned(
                    YearMonth.of(2019, 3),
                    klassifiserteInntekter =
                        listOf(
                            KlassifisertInntekt(
                                beløp = BigDecimal(1000000),
                                inntektKlasse = InntektKlasse.ARBEIDSINNTEKT,
                            ),
                            KlassifisertInntekt(
                                beløp = BigDecimal(-950000),
                                inntektKlasse = InntektKlasse.ARBEIDSINNTEKT,
                            ),
                        ),
                ),
            )

        val beregningsdato = LocalDate.of(2019, 5, 10)
        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        assertEquals(50000.toBigDecimal(), fakta.arbeidsinntektSiste12)

        val evaluering = ordinærSiste12Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom summen av inntekt blir negativ`() {
        val inntekt =
            listOf(
                KlassifisertInntektMåned(
                    YearMonth.of(2019, 3),
                    klassifiserteInntekter =
                        listOf(
                            KlassifisertInntekt(
                                beløp = BigDecimal(1000000),
                                inntektKlasse = InntektKlasse.ARBEIDSINNTEKT,
                            ),
                            KlassifisertInntekt(
                                beløp = BigDecimal(-1950000),
                                inntektKlasse = InntektKlasse.ARBEIDSINNTEKT,
                            ),
                        ),
                ),
            )

        val beregningsdato = LocalDate.of(2019, 5, 10)
        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = false,
                fangstOgFiske = false,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        assertEquals((-950000).toBigDecimal(), fakta.arbeidsinntektSiste12)

        val evaluering = ordinærSiste12Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    fun generateFangstOgFiske(
        range: IntRange,
        beløpPerMnd: BigDecimal,
    ): List<KlassifisertInntektMåned> {
        return (range).toList().map {
            KlassifisertInntektMåned(
                YearMonth.of(2019, 1).minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(
                        beløpPerMnd,
                        InntektKlasse.FANGST_FISKE,
                    ),
                ),
            )
        }
    }

    fun generateArbeidsInntekt(
        range: IntRange,
        beløpPerMnd: BigDecimal,
    ): List<KlassifisertInntektMåned> {
        return (range).toList().map {
            KlassifisertInntektMåned(
                YearMonth.of(2019, 1).minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(
                        beløpPerMnd,
                        InntektKlasse.ARBEIDSINNTEKT,
                    ),
                ),
            )
        }
    }

    fun generate12MånederArbeidsInntekt(): List<KlassifisertInntektMåned> {
        return generateArbeidsInntekt(1..12, BigDecimal(50000))
    }

    fun generate36MånederArbeidsInntekt(): List<KlassifisertInntektMåned> {
        return generateArbeidsInntekt(1..36, BigDecimal(50000))
    }
}
