package no.nav.dagpenger.regel.minsteinntekt.inngangsvilkårKorona

import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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

class InngangsvilkårKoronaTest {

    val G2019 = BigDecimal(99858)

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har tjent under 0,75G arbeidsinntekt siste 12 mnd`() {

        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2019, 4),
                listOf(
                    KlassifisertInntekt(G2019 * BigDecimal(1), InntektKlasse.FANGST_FISKE), KlassifisertInntekt(
                        BigDecimal(20), InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2020, 2, 10)
        )

        val evaluering = kravTilMinsteinntektKorona.evaluer(fakta)
        assertEquals(Resultat.NEI, evaluering.resultat)

        val riktigRegel = finnEvaluering(
            evaluering,
            "Krav til minsteinntekt etter midlertidig korona-endret § 4-4 første ledd bokstav a"
        )

        assertNotNull(riktigRegel, "Brukte ikke riktig regel")
        assertEquals(Resultat.NEI, riktigRegel.resultat)
        assertEquals(Beregningsregel.KORONA, evaluering.finnRegelBrukt())
    }

    @Test
    fun `Skal gi rett til dagpenger i følge § 4-4 dersom du har hatt over 0,75G siste 12 mnd`() {

        val inntekt = generateArbeidsInntekt(1..1, G2019)

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2020, 2, 10)
        )

        val evaluering = kravTilMinsteinntektKorona.evaluer(fakta)
        assertEquals(Resultat.JA, evaluering.resultat)

        val riktigRegel = finnEvaluering(
            evaluering,
            "Krav til minsteinntekt etter midlertidig korona-endret § 4-4 første ledd bokstav a"
        )

        assertNotNull(riktigRegel, "Brukte ikke riktig regel")
        assertEquals(Resultat.JA, riktigRegel.resultat)
        assertEquals(Beregningsregel.KORONA, evaluering.finnRegelBrukt())
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har tjent under 2,25G arbeidsinntekt siste 36 mnd`() {

        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 4),
                listOf(
                    KlassifisertInntekt(G2019 * BigDecimal(2), InntektKlasse.FANGST_FISKE), KlassifisertInntekt(
                        G2019 * BigDecimal(2), InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2020, 2, 10)
        )

        val evaluering = kravTilMinsteinntektKorona.evaluer(fakta)
        assertEquals(Resultat.NEI, evaluering.resultat)

        val riktigRegel = finnEvaluering(
            evaluering,
            "Krav til minsteinntekt etter midlertidig korona-endret § 4-4 første ledd bokstav b"
        )

        assertNotNull(riktigRegel, "Brukte ikke riktig regel")
        assertEquals(Resultat.NEI, riktigRegel.resultat)
        assertEquals(Beregningsregel.KORONA, evaluering.finnRegelBrukt())
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har tjent over 2,25G siste 36 mnd`() {

        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 1),
                listOf(KlassifisertInntekt(G2019 * BigDecimal(2.5), InntektKlasse.ARBEIDSINNTEKT))
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2020, 2, 10)
        )

        val evaluering = kravTilMinsteinntektKorona.evaluer(fakta)
        assertEquals(Resultat.JA, evaluering.resultat)

        val riktigRegel = finnEvaluering(
            evaluering,
            "Krav til minsteinntekt etter midlertidig korona-endret § 4-4 første ledd bokstav b"
        )

        assertNotNull(riktigRegel, "Brukte ikke riktig regel")
        assertEquals(Resultat.JA, riktigRegel.resultat)
        assertEquals(Beregningsregel.KORONA, evaluering.finnRegelBrukt())
    }

    fun generateArbeidsInntekt(range: IntRange, beløpPerMnd: BigDecimal): List<KlassifisertInntektMåned> {
        return (range).toList().map {
            KlassifisertInntektMåned(
                YearMonth.of(2020, 2).minusMonths(it.toLong()), listOf(
                    KlassifisertInntekt(
                        beløpPerMnd, InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        }
    }
}
