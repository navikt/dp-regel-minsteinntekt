package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth

class OppfyllerKravTilMinsteinntektTest {

    fun generate36MånederArbeidsInntekt(): List<KlassifisertInntektMåned> {
        return (1..36).toList().map {
            KlassifisertInntektMåned(YearMonth.of(2019, 1).minusMonths(it.toLong()), listOf(KlassifisertInntekt(BigDecimal(50000), InntektKlasse.ARBEIDSINNTEKT)))
        }
    }

    @Test
    fun `Skal oppfylle krav til minsteinntekt ved verneplikt`() {
        val resultat = oppfyllerKravTilMinsteinntekt(
            true,
            Inntekt("123", emptyList()),
            YearMonth.of(2019, 4),
            null,
            false)

        assert(resultat)
    }

    @Test
    fun `Skal ikke oppfylle krav til minsteinntekt uten inntekt`() {
        val resultat = oppfyllerKravTilMinsteinntekt(
            false,
            Inntekt("123", emptyList()),
            YearMonth.of(2019, 4),
            null,
            false)
        assertFalse(resultat)
    }

    @Test
    fun `Skal få oppfylt minsteinntekt med mellom 1,5 og 2G inntekt siste 12 mnd`() {

        val inntektsListe = (1..30).toList().map {
            KlassifisertInntektMåned(
                YearMonth.now().minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(14500),
                        InntektKlasse.ARBEIDSINNTEKT)))
        }

        val resultat = oppfyllerKravTilMinsteinntekt(
            false,
            Inntekt("123", inntektsListe),
            YearMonth.now().minusMonths(1),
            null,
            false)
        assertTrue(resultat)
    }

    @Test
    fun `Skal få oppfylt minsteinntekt med næringsinntekt siste 12 mnd`() {

        val inntektsListe = (1..30).toList().map {
            KlassifisertInntektMåned(
                YearMonth.now().minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(14500),
                        InntektKlasse.FANGST_FISKE)))
        }

        val resultat = oppfyllerKravTilMinsteinntekt(
            false,
            Inntekt("123", inntektsListe),
            YearMonth.now().minusMonths(1),
            null,
            true)
        assertTrue(resultat)
    }

    @Test
    fun `Skal ikke få oppfylt med næringsinntekt dersom fangst og fisk er false `() {

        val inntektsListe = (1..30).toList().map {
            KlassifisertInntektMåned(
                YearMonth.now().minusMonths(it.toLong()),
                listOf(
                    KlassifisertInntekt(
                        BigDecimal(14500),
                        InntektKlasse.FANGST_FISKE)))
        }

        val resultat = oppfyllerKravTilMinsteinntekt(
            false,
            Inntekt("123", inntektsListe),
            YearMonth.now().minusMonths(1),
            null,
            false)
        assertFalse(resultat)
    }

    @Test
    fun `Skal oppfylle krav til minsteinntekt ved nok arbeidsinntekt`() {
        val resultat = oppfyllerKravTilMinsteinntekt(
            false,
            Inntekt("123", generate36MånederArbeidsInntekt()),
            YearMonth.of(2019, 2),
            null,
            false
        )

        assertTrue(resultat)
    }

    @Test
    fun `Hvis tidligere brukte inntekter finnes skal de ikke taes med`() {
        val resultat = oppfyllerKravTilMinsteinntekt(
            false,
            Inntekt("123", generate36MånederArbeidsInntekt()),
            YearMonth.of(2019, 2),
            InntektsPeriode(YearMonth.of(2015, 1), YearMonth.of(2018, 10)),
            false
        )

        assertFalse(resultat)
    }
}