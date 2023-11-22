package no.nav.dagpenger.regel.minsteinntekt.inngangsvilkårOrdinær

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Fakta
import no.nav.dagpenger.regel.minsteinntekt.fangstOgFisk
import no.nav.dagpenger.regel.minsteinntekt.generate12MånederFangstOgFiskInntekt
import no.nav.dagpenger.regel.minsteinntekt.generateArbeidsOgFangstOgFiskInntekt
import no.nav.dagpenger.regel.minsteinntekt.generateArbeidsinntekt
import no.nav.dagpenger.regel.minsteinntekt.generateFangstOgFiskInntekt
import no.nav.dagpenger.regel.minsteinntekt.grunnbeløpStrategy
import no.nav.dagpenger.regel.minsteinntekt.ordinærSiste12Måneder
import no.nav.dagpenger.regel.minsteinntekt.ordinærSiste12MånederMedFangstOgFiske
import no.nav.dagpenger.regel.minsteinntekt.ordinærSiste36MånederMedFangstOgFiske
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

internal class InngangsvilkårFangstOgFiskeSpesifikasjonsTest {
    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du ikke har inntekt siste 12 mnd`() {
        val beregningsdato = LocalDate.of(2019, 5, 10)
        val fakta =
            Fakta(
                inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = true,
                fangstOgFiske = true,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske.evaluer(fakta)
        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har tjent litt for lite siste 12 mnd`() {
        val inntekt = generateFangstOgFiskInntekt(3, BigDecimal(1))

        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = true,
                fangstOgFiske = true,
                beregningsdato = LocalDate.of(2019, 5, 10),
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = BigDecimal(4),
            )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske.evaluer(fakta)

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
                            inntektKlasse = InntektKlasse.FANGST_FISKE,
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
                fangstOgFiske = true,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        assertEquals(50000.toBigDecimal(), fakta.inntektSiste12inkludertFangstOgFiske)

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
                            inntektKlasse = InntektKlasse.FANGST_FISKE,
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
                fangstOgFiske = true,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        assertEquals((-950000).toBigDecimal(), fakta.inntektSiste12inkludertFangstOgFiske)

        val evaluering = ordinærSiste12Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal gi rett til dagpenger i følge § 4-4 dersom du har hatt nok inntekt siste 12 mnd`() {
        val inntekt = generate12MånederFangstOgFiskInntekt()

        val beregningsdato = LocalDate.of(2019, 5, 10)
        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = true,
                fangstOgFiske = true,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske.evaluer(fakta)

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
                fangstOgFiske = true,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har tjent litt for lite siste 36 mnd`() {
        val inntekt = generateFangstOgFiskInntekt(24, BigDecimal(1))

        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
                bruktInntektsperiode = null,
                verneplikt = true,
                fangstOgFiske = true,
                beregningsdato = LocalDate.of(2019, 5, 10),
                regelverksdato = LocalDate.of(2019, 5, 10),
                grunnbeløp = BigDecimal(23),
            )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @ParameterizedTest
    @CsvSource(
        "2021-12-31, JA",
        "2022-01-01, NEI",
    )
    fun `Regelverk for fangst og fisk er avviklet fra 01-01-2022`(
        regelverksdato: String,
        forventetUtfall: String,
    ) {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2021, 11)

        val inntekt =
            generateArbeidsOgFangstOgFiskInntekt(
                numberOfMonths = 36,
                arbeidsInntektBeløpPerMnd = BigDecimal(1000),
                fangstOgFiskeBeløpPerMnd = BigDecimal(50000),
                senesteMåned = sisteAvsluttendeKalenderMåned,
            )

        val beregningsdato = LocalDate.parse(regelverksdato)
        val fakta =
            Fakta(
                inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned),
                bruktInntektsperiode = null,
                verneplikt = true,
                fangstOgFiske = true,
                beregningsdato = beregningsdato,
                regelverksdato = LocalDate.parse(regelverksdato),
                grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
            )

        val evaluering = fangstOgFisk.evaluer(fakta)

        assertTrue(evaluering.children.all { Resultat.valueOf(forventetUtfall) == it.resultat })
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom man har næringsinntekt siste 36 mnd, men er fangst og fisk er ikke oppfylt `() {
        val inntekt = generateArbeidsinntekt(36, BigDecimal(50000))

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

        val evaluering = ordinærSiste36MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }
}
