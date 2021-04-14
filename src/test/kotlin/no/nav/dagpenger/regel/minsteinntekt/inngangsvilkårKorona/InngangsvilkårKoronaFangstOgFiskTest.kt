package no.nav.dagpenger.regel.minsteinntekt.inngangsvilkårKorona

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Beregningsregel
import no.nav.dagpenger.regel.minsteinntekt.Fakta
import no.nav.dagpenger.regel.minsteinntekt.finnRegelBrukt
import no.nav.dagpenger.regel.minsteinntekt.kravTilMinsteinntektKorona
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class InngangsvilkårKoronaFangstOgFiskTest() {
    val G2019 = BigDecimal(99858)

    @Test
    fun `Skal gi rett til dagpenger i følge § 4-4 dersom du har mer enn 2,25G fangst og fiske-inntekt siste 36 mnd, og fangstOgFisk er satt`() {

        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 1),
                listOf(KlassifisertInntekt(G2019 * BigDecimal(2.5), InntektKlasse.FANGST_FISKE))
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2020, 2, 10),
            regelverksdato = LocalDate.of(2020, 2, 10)
        )

        val evaluering = kravTilMinsteinntektKorona.evaluer(fakta)
        assertEquals(Resultat.JA, evaluering.resultat)

        val riktigRegel = finnEvaluering(
            evaluering,
            "Krav til minsteinntekt etter § 4-18 + midlertidig korona-endret § 4-4 første ledd bokstav b"
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
                listOf(KlassifisertInntekt(G2019, InntektKlasse.FANGST_FISKE))
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2020, 2, 10),
            regelverksdato = LocalDate.of(2020, 2, 10)
        )

        val evaluering = kravTilMinsteinntektKorona.evaluer(fakta)
        assertEquals(Resultat.JA, evaluering.resultat)

        val riktigRegel = finnEvaluering(
            evaluering,
            "Krav til minsteinntekt etter § 4-18 + midlertidig korona-endret § 4-4 første ledd bokstav b"
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
                listOf(KlassifisertInntekt(G2019 * BigDecimal(2), InntektKlasse.FANGST_FISKE))
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2020, 2, 10),
            regelverksdato = LocalDate.of(2020, 2, 10)
        )

        val evaluering = kravTilMinsteinntektKorona.evaluer(fakta)
        assertEquals(Resultat.NEI, evaluering.resultat)

        val riktigRegel = finnEvaluering(
            evaluering,
            "Krav til minsteinntekt etter § 4-18 + midlertidig korona-endret § 4-4 første ledd bokstav b"
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
                listOf(KlassifisertInntekt(G2019 * BigDecimal(0.5), InntektKlasse.FANGST_FISKE))
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2020, 2, 10),
            regelverksdato = LocalDate.of(2020, 2, 10)
        )

        val evaluering = kravTilMinsteinntektKorona.evaluer(fakta)
        assertEquals(Resultat.NEI, evaluering.resultat)

        val riktigRegel = finnEvaluering(
            evaluering,
            "Krav til minsteinntekt etter § 4-18 + midlertidig korona-endret § 4-4 første ledd bokstav b"
        )

        assertNotNull(riktigRegel, "Brukte ikke riktig regel")
        assertEquals(Resultat.NEI, riktigRegel.resultat)
        assertEquals(Beregningsregel.KORONA, evaluering.finnRegelBrukt())
    }
}
