package no.nav.dagpenger.regel.minsteinntekt.inngangsvilkårKorona

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Beregningsregel
import no.nav.dagpenger.regel.minsteinntekt.Fakta
import no.nav.dagpenger.regel.minsteinntekt.finnRegelBrukt
import no.nav.dagpenger.regel.minsteinntekt.generateArbeidsOgFangstOgFiskInntekt
import no.nav.dagpenger.regel.minsteinntekt.grunnbeløpStrategy
import no.nav.dagpenger.regel.minsteinntekt.koronaFangstOgFisk
import no.nav.dagpenger.regel.minsteinntekt.kravTilMinsteinntektKorona
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class InngangsvilkårKoronaFangstOgFiskTest() {
    @Suppress("ktlint:standard:property-naming")
    val G2019 = BigDecimal(99858)

    @ParameterizedTest
    @CsvSource(
        "2021-12-31, JA",
        "2022-01-01, NEI",
    )
    fun `Koronaregelverk for fangst og fisk er avviklet fra 01-01-2022`(
        regelverksdato: String,
        forventetUtfall: String,
    ) {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2021, 11)

        val inntekt = generateArbeidsOgFangstOgFiskInntekt(
            numberOfMonths = 36,
            arbeidsInntektBeløpPerMnd = BigDecimal(1000),
            fangstOgFiskeBeløpPerMnd = BigDecimal(50000),
            senesteMåned = sisteAvsluttendeKalenderMåned,
        )

        val beregningsdato = LocalDate.parse(regelverksdato)
        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned),
            bruktInntektsperiode = null,
            verneplikt = true,
            fangstOgFiske = true,
            beregningsdato = beregningsdato,
            regelverksdato = LocalDate.parse(regelverksdato),
            grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
        )

        val evaluering = koronaFangstOgFisk.evaluer(fakta)

        assertTrue(evaluering.children.all { Resultat.valueOf(forventetUtfall) == it.resultat })
    }

    @Test
    fun `Skal gi rett til dagpenger i følge § 4-4 dersom du har mer enn 2,25G fangst og fiske-inntekt siste 36 mnd, og fangstOgFisk er satt`() {
        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 1),
                listOf(KlassifisertInntekt(G2019 * BigDecimal(2.5), InntektKlasse.FANGST_FISKE)),
            ),
        )

        val beregningsdato = LocalDate.of(2020, 2, 10)
        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)),
            bruktInntektsperiode = null,
            verneplikt = false,
            fangstOgFiske = true,
            beregningsdato = beregningsdato,
            regelverksdato = LocalDate.of(2020, 2, 10),
            grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
        )

        val evaluering = kravTilMinsteinntektKorona.evaluer(fakta)
        assertEquals(Resultat.JA, evaluering.resultat)

        val riktigRegel = finnEvaluering(
            evaluering,
            "Krav til minsteinntekt etter § 4-18 + midlertidig korona-endret § 4-4 første ledd bokstav b",
        )

        assertNotNull(riktigRegel, "Brukte ikke riktig regel")
        assertEquals(Resultat.JA, riktigRegel.resultat)

        assertEquals(Beregningsregel.KORONA, evaluering.finnRegelBrukt())
    }

    @Test
    fun `Skal gi rett til dagpenger i følge § 4-4 dersom du har mer enn 0,75G fangst og fiske-inntekt siste 12 mnd, og fangstOgFisk er satt`() {
        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2020, 1),
                listOf(KlassifisertInntekt(G2019, InntektKlasse.FANGST_FISKE)),
            ),
        )

        val beregningsdato = LocalDate.of(2020, 2, 10)
        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)),
            bruktInntektsperiode = null,
            verneplikt = false,
            fangstOgFiske = true,
            beregningsdato = beregningsdato,
            regelverksdato = LocalDate.of(2020, 2, 10),
            grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
        )

        val evaluering = kravTilMinsteinntektKorona.evaluer(fakta)
        assertEquals(Resultat.JA, evaluering.resultat)

        val riktigRegel = finnEvaluering(
            evaluering,
            "Krav til minsteinntekt etter § 4-18 + midlertidig korona-endret § 4-4 første ledd bokstav b",
        )

        assertNotNull(riktigRegel, "Brukte ikke riktig regel")
        assertEquals(Resultat.JA, riktigRegel.resultat)
        assertEquals(Beregningsregel.KORONA, evaluering.finnRegelBrukt())
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har mindre enn 2,25G fangst og fiske-inntekt siste 36 mnd, og fangstOgFisk er satt`() {
        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 1),
                listOf(KlassifisertInntekt(G2019 * BigDecimal(2), InntektKlasse.FANGST_FISKE)),
            ),
        )

        val beregningsdato = LocalDate.of(2020, 2, 10)
        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)),
            bruktInntektsperiode = null,
            verneplikt = false,
            fangstOgFiske = true,
            beregningsdato = beregningsdato,
            regelverksdato = LocalDate.of(2020, 2, 10),
            grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
        )

        val evaluering = kravTilMinsteinntektKorona.evaluer(fakta)
        assertEquals(Resultat.NEI, evaluering.resultat)

        val riktigRegel = finnEvaluering(
            evaluering,
            "Krav til minsteinntekt etter § 4-18 + midlertidig korona-endret § 4-4 første ledd bokstav b",
        )

        assertNotNull(riktigRegel, "Brukte ikke riktig regel")
        assertEquals(Resultat.NEI, riktigRegel.resultat)
        assertEquals(Beregningsregel.KORONA, evaluering.finnRegelBrukt())
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har mindre enn 0,75G fangst og fiske-inntekt siste 12 mnd, og fangstOgFisk er satt`() {
        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2020, 1),
                listOf(KlassifisertInntekt(G2019 * BigDecimal(0.5), InntektKlasse.FANGST_FISKE)),
            ),
        )

        val beregningsdato = LocalDate.of(2020, 2, 10)
        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)),
            bruktInntektsperiode = null,
            verneplikt = false,
            fangstOgFiske = true,
            beregningsdato = beregningsdato,
            regelverksdato = LocalDate.of(2020, 2, 10),
            grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
        )

        val evaluering = kravTilMinsteinntektKorona.evaluer(fakta)
        assertEquals(Resultat.NEI, evaluering.resultat)

        val riktigRegel = finnEvaluering(
            evaluering,
            "Krav til minsteinntekt etter § 4-18 + midlertidig korona-endret § 4-4 første ledd bokstav b",
        )

        assertNotNull(riktigRegel, "Brukte ikke riktig regel")
        assertEquals(Resultat.NEI, riktigRegel.resultat)
        assertEquals(Beregningsregel.KORONA, evaluering.finnRegelBrukt())
    }
}
