package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertNotNull

class CreateInntektPerioderTest {
    val minsteinntekt = Minsteinntekt(Configuration())

    @Test
    fun `createInntektPerioder correctly for no inntekt`() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)
        val fakta = Fakta(
            Inntekt("id", emptyList(), sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned),
            null,
            false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 4, 10)
        )

        val inntektsPerioder = minsteinntekt.createInntektPerioder(fakta)
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)

        assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal.ZERO })
        assertTrue(inntektsPerioder.all { it.andel == BigDecimal.ZERO })
        assertTrue(inntektsPerioder.none { it.inneholderFangstOgFisk })
    }

    @Test
    fun `createInntektPerioder correctly for constant arbeidsinntekt`() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val inntektsListe = generateArbeidsinntekt(36, BigDecimal(1000), sisteAvsluttendeKalenderMåned)
        val fakta = Fakta(
            Inntekt(
                "id",
                inntektsListe,
                sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned
            ),
            null,
            false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 2, 10)
        )

        val inntektsPerioder = minsteinntekt.createInntektPerioder(fakta)
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)

        assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(12000) })
        assertTrue(inntektsPerioder.all { it.andel == BigDecimal(12000) })
        assertTrue(inntektsPerioder.none { it.inneholderFangstOgFisk })
    }

    @Test
    fun `createInntektPerioder correctly for inntekt with both types when fangstOgFiske=false`() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val inntektsListe = generateArbeidsOgFangstOgFiskInntekt(
            36,
            BigDecimal(2000),
            BigDecimal(2000),
            sisteAvsluttendeKalenderMåned
        )
        val fakta = Fakta(
            Inntekt(
                "id",
                inntektsListe,
                sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned
            ),
            null,
            false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 2, 10)
        )

        val inntektsPerioder = minsteinntekt.createInntektPerioder(fakta)
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)

        assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(24000) })
        assertTrue(inntektsPerioder.all { it.andel == BigDecimal(24000) })
        assertTrue(inntektsPerioder.all { it.inneholderFangstOgFisk })
    }

    @Test
    fun `createInntektPerioder correctly for inntekt with both types when fangstOgFiske=true`() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val inntektsListe =
            generateArbeidsOgFangstOgFiskInntekt(36, BigDecimal(2000), BigDecimal(2000), sisteAvsluttendeKalenderMåned)
        val fakta = Fakta(
            Inntekt(
                "id",
                inntektsListe,
                sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned
            ),
            null,
            false,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2019, 2, 10)
        )

        val inntektsPerioder = minsteinntekt.createInntektPerioder(fakta)
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)

        assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(48000) })
        assertTrue(inntektsPerioder.all { it.andel == BigDecimal(48000) })
        assertTrue(inntektsPerioder.all { it.inneholderFangstOgFisk })
    }

    @Test
    fun `createInntektPerioder correctly for constant fangstogfiskeinntekt`() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 1)
        val inntektsListe = generateFangstOgFiskInntekt(36, BigDecimal(2000), sisteAvsluttendeKalenderMåned)
        val fakta = Fakta(
            Inntekt("id", inntektsListe, sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned),
            null,
            false,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2019, 2, 10)
        )

        val inntektsPerioder = minsteinntekt.createInntektPerioder(fakta)
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)

        assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(24000) })
        assertTrue(inntektsPerioder.all { it.andel == BigDecimal(24000) })
        assertTrue(inntektsPerioder.all { it.inneholderFangstOgFisk })
    }

    @Test
    fun `createInntektPerioder correctly excludes brukt inntekt`() {
        val sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 3)

        val inntektsListe = generateArbeidsinntekt(36, BigDecimal(2000), sisteAvsluttendeKalenderMåned)
        val fakta = Fakta(
            Inntekt("id", inntektsListe, sisteAvsluttendeKalenderMåned = sisteAvsluttendeKalenderMåned),
            InntektsPeriode(YearMonth.of(2015, 1), YearMonth.of(2017, 7)),
            false,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 4, 10)
        )

        val inntektsPerioder = minsteinntekt.createInntektPerioder(fakta)
        assertThreeCorrectPeriods(inntektsPerioder, sisteAvsluttendeKalenderMåned)

        assertTrue(inntektsPerioder.all { it.inntekt == BigDecimal(24000) })
        assertEquals(BigDecimal(24000), inntektsPerioder.find { it.periode == 1 }?.andel)
        assertEquals(BigDecimal(16000), inntektsPerioder.find { it.periode == 2 }?.andel)
        assertEquals(BigDecimal.ZERO, inntektsPerioder.find { it.periode == 3 }?.andel)
        assertTrue(inntektsPerioder.none { it.inneholderFangstOgFisk })
    }

    fun assertThreeCorrectPeriods(inntektsInfoListe: List<InntektPeriodeInfo>, senesteMåned: YearMonth) {
        assertEquals(3, inntektsInfoListe.size)

        val førstePeriode = inntektsInfoListe.find { it.periode == 1 }
        val andrePeriode = inntektsInfoListe.find { it.periode == 2 }
        val tredjePeriode = inntektsInfoListe.find { it.periode == 3 }

        assertNotNull(førstePeriode)
        assertNotNull(andrePeriode)
        assertNotNull(tredjePeriode)

        assertEquals(senesteMåned, førstePeriode.inntektsPeriode.sisteMåned)
        assertEquals(senesteMåned.minusYears(1).plusMonths(1), førstePeriode.inntektsPeriode.førsteMåned)
        assertEquals(senesteMåned.minusYears(1), andrePeriode.inntektsPeriode.sisteMåned)
        assertEquals(senesteMåned.minusYears(2).plusMonths(1), andrePeriode.inntektsPeriode.førsteMåned)
        assertEquals(senesteMåned.minusYears(2), tredjePeriode.inntektsPeriode.sisteMåned)
        assertEquals(senesteMåned.minusYears(3).plusMonths(1), tredjePeriode.inntektsPeriode.førsteMåned)
    }
}
