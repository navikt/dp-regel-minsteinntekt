package no.nav.dagpenger.regel.minsteinntekt

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class OppfyllerKravTilMinsteinntektTest {

    @Test
    fun `Skal oppfylle krav til minsteinntekt ved verneplikt`() {
        val resultat = oppfyllerKravTilMinsteinntekt(true, 0)
        assert(resultat)
    }

    @Test
    fun `Skal ikke oppfylle krav til minsteinntekt uten inntekt`() {
        val resultat = oppfyllerKravTilMinsteinntekt(false, 0)
        assertFalse(resultat)
    }
}