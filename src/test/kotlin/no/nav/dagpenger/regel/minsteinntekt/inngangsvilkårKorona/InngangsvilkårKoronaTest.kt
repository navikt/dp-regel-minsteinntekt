package no.nav.dagpenger.regel.minsteinntekt.inngangsvilkårKorona

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Fakta
import no.nav.dagpenger.regel.minsteinntekt.kravTilMinsteinntektKorona
import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class InngangsvilkårKoronaTest {

    val G2019 = BigDecimal(99858)

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har tjent under 0,75G siste 12 mnd`() {

        val inntekt = generateArbeidsInntekt(1..3, BigDecimal(1))

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
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har tjent under 2,25G siste 36 mnd`() {

        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 1),
                listOf(KlassifisertInntekt(G2019* BigDecimal(2), InntektKlasse.ARBEIDSINNTEKT)))
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
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har tjent over 2,25G siste 36 mnd`() {

        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 1),
                listOf(KlassifisertInntekt(G2019*BigDecimal(2.5), InntektKlasse.ARBEIDSINNTEKT)))
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
    }


    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har mer enn 2,25G  siste 36 mnd, men fangstOgFisk er ikke satt`() {

        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2018, 1),
                listOf(KlassifisertInntekt(G2019* BigDecimal(2), InntektKlasse.ARBEIDSINNTEKT)))
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
    }
    /*

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom man har næringsinntekt siste 12 mnd, men er fangst og fisk er ikke oppfylt `() {

        val inntekt = generateFangstOgFiske(1..12, BigDecimal(50000))

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 5, 10)
        )

        val evaluering = ordinærSiste12Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom man har næringsinntekt siste 36 mnd, men er fangst og fisk er ikke oppfylt `() {

        val inntekt = generateFangstOgFiske(1..36, BigDecimal(50000))

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 5, 10)
        )

        val evaluering = ordinærSiste36Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du ikke hatt nok inntekt siste 12 mnd, på grunn av minus-inntekt`() {

        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2019, 3), klassifiserteInntekter = listOf(
                    KlassifisertInntekt(
                        beløp = BigDecimal(1000000),
                        inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                    ),
                    KlassifisertInntekt(
                        beløp = BigDecimal(-950000),
                        inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 5, 10)
        )

        assertEquals(50000.toBigDecimal(), fakta.arbeidsinntektSiste12)

        val evaluering = ordinærSiste12Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom summen av inntekt blir negativ`() {

        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2019, 3), klassifiserteInntekter = listOf(
                    KlassifisertInntekt(
                        beløp = BigDecimal(1000000),
                        inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                    ),
                    KlassifisertInntekt(
                        beløp = BigDecimal(-1950000),
                        inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 5, 10)
        )

        assertEquals((-950000).toBigDecimal(), fakta.arbeidsinntektSiste12)

        val evaluering = ordinærSiste12Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }
     */

    fun generateFangstOgFiske(range: IntRange, beløpPerMnd: BigDecimal): List<KlassifisertInntektMåned> {
        return (range).toList().map {
            KlassifisertInntektMåned(YearMonth.of(2019, 1).minusMonths(it.toLong()), listOf(KlassifisertInntekt(
                beløpPerMnd, InntektKlasse.FANGST_FISKE)))
        }
    }

    fun generateArbeidsInntekt(range: IntRange, beløpPerMnd: BigDecimal): List<KlassifisertInntektMåned> {
        return (range).toList().map {
            KlassifisertInntektMåned(YearMonth.of(2020, 2).minusMonths(it.toLong()), listOf(KlassifisertInntekt(
                beløpPerMnd, InntektKlasse.ARBEIDSINNTEKT)))
        }
    }

    fun generate12MånederArbeidsInntekt(): List<KlassifisertInntektMåned> {
        return generateArbeidsInntekt(1..12, BigDecimal(50000))
    }

    fun generate36MånederArbeidsInntekt(): List<KlassifisertInntektMåned> {
        return generateArbeidsInntekt(1..36, BigDecimal(50000))
    }

    fun finnEvaluering(evaluering: Evaluering, string: String): Evaluering? {
        if (evaluering.identifikator == string) {
            return evaluering
        }
        evaluering.children.forEach {
            if (finnEvaluering(it, string) != null) {
                return it
            }
        }
        return null
    }
}