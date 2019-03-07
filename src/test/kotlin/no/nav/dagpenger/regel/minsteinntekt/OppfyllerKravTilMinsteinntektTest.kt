package no.nav.dagpenger.regel.minsteinntekt

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth

class OppfyllerKravTilMinsteinntektTest {

    @Test
    fun `Skal oppfylle krav til minsteinntekt ved verneplikt`() {
        val resultat = oppfyllerKravTilMinsteinntekt(
            true,
            Inntekt("123", emptyList()),
            YearMonth.of(2019, 4),
            false)

        assert(resultat)
    }

    @Test
    fun `Skal ikke oppfylle krav til minsteinntekt uten inntekt`() {
        val resultat = oppfyllerKravTilMinsteinntekt(
            false,
            Inntekt("123", emptyList()),
            YearMonth.of(2019, 4),
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
                        InntektKlasse.NÆRINGSINNTEKT)))
        }

        val resultat = oppfyllerKravTilMinsteinntekt(
            false,
            Inntekt("123", inntektsListe),
            YearMonth.now().minusMonths(1),
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
                        InntektKlasse.NÆRINGSINNTEKT)))
        }

        val resultat = oppfyllerKravTilMinsteinntekt(
            false,
            Inntekt("123", inntektsListe),
            YearMonth.now().minusMonths(1),
            false)
        assertFalse(resultat)
    }
}