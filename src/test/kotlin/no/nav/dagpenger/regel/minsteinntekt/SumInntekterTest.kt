package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth
import kotlin.test.assertEquals

class SumInntekterTest {

    fun generateSiste36MånederArbeidsInntekt(): List<KlassifisertInntektMåned> {

        return (1..36).toList().map {
            KlassifisertInntektMåned(YearMonth.now().minusMonths(it.toLong()), listOf(KlassifisertInntekt(BigDecimal(1000), InntektKlasse.ARBEIDSINNTEKT)))
        }
    }

    fun generateSiste36MånederNæringsInntekt(): List<KlassifisertInntektMåned> {

        return (1..36).toList().map {
            KlassifisertInntektMåned(YearMonth.now().minusMonths(it.toLong()), listOf(KlassifisertInntekt(BigDecimal(1000), InntektKlasse.FANGST_FISKE)))
        }
    }

    @Test
    fun ` should add Arbeidsinntekt in sumSiste12 `() {

        assertEquals(BigDecimal(12000), sumArbeidsInntekt(generateSiste36MånederArbeidsInntekt(), YearMonth.now().minusMonths(1), 11))
    }

    @Test
    fun ` should not add næringsinntekt in sumSiste12 when we use sumArbeidsInntekt `() {

        assertEquals(BigDecimal(0), sumArbeidsInntekt(generateSiste36MånederNæringsInntekt(), YearMonth.now().minusMonths(1), 11))
    }

    @Test
    fun ` should add næringsinntekt in sumSiste12 when we use sumNæringsInntekt `() {

        assertEquals(BigDecimal(12000),
            sumNæringsInntekt(
                generateSiste36MånederNæringsInntekt(),
                YearMonth.now().minusMonths(1),
                11)
        )
    }

    @Test
    fun ` should add not add Arbeidsinntekt in sumSiste12 when we use sumNæringsInntekt`() {

        assertEquals(BigDecimal(0),
            sumNæringsInntekt(
                generateSiste36MånederArbeidsInntekt(),
                YearMonth.now().minusMonths(1),
                11)
        )
    }
}