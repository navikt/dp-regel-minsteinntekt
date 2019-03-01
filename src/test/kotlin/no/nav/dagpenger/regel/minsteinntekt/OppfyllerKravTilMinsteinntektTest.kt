package no.nav.dagpenger.regel.minsteinntekt

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.YearMonth

class OppfyllerKravTilMinsteinntektTest {

    @Test
    fun `Skal oppfylle krav til minsteinntekt ved verneplikt`() {
        val resultat = oppfyllerKravTilMinsteinntekt(
            true,
            Inntekt("123", emptyList()),
            YearMonth.of(2019, 4))

        assert(resultat)
    }

    @Test
    fun `Skal ikke oppfylle krav til minsteinntekt uten inntekt`() {
        val resultat = oppfyllerKravTilMinsteinntekt(
            false,
            Inntekt("123", emptyList()),
            YearMonth.of(2019, 4))
        assertFalse(resultat)
    }
}