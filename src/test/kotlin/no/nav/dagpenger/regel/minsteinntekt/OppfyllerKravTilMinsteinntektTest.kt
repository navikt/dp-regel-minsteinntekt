package no.nav.dagpenger.regel.minsteinntekt

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

    @Test
    fun `Skal oppfylle krav til minsteinntekt ved nok arbeidsinntekt`() {
        val resultat = oppfyllerKravTilMinsteinntekt(
            false,
            Inntekt("123", generate36MånederArbeidsInntekt()),
            YearMonth.of(2019, 2)
        )

        assertTrue(resultat)
    }

    @Test
    fun `Hvis tidligere brukte inntekter finnes skal de ikke taes med`() {
        val resultat = oppfyllerKravTilMinsteinntekt(
            false,
            Inntekt("123", generate36MånederArbeidsInntekt()),
            YearMonth.of(2019, 2),
            InntektsPeriode(YearMonth.of(2015, 1), YearMonth.of(2018, 10 ))
        )

        assertFalse(resultat)
    }
}