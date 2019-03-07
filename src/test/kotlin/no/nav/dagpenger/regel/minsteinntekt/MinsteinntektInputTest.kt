package no.nav.dagpenger.regel.minsteinntekt

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.YearMonth

class MinsteinntektInputTest {

    @Test
    fun `Process behov without inntekt and no inntekt tasks`() {
        val behov = SubsumsjonsBehov.Builder().build()

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun `Process behov without inntekt and no hentInntekt task`() {
        val behov = SubsumsjonsBehov.Builder()
                .task(listOf("noe annet"))
            .build()

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun `Do not process behov without inntekt but with hentInntekt task`() {
        val behov = SubsumsjonsBehov.Builder()
            .task(listOf("hentInntekt"))
            .build()
        assertFalse(shouldBeProcessed(behov))
    }

    @Test
    fun `Process behov with inntekt`() {

        val behov = SubsumsjonsBehov.Builder()
            .inntekt(Inntekt("123", emptyList()))
            .senesteInntektsmåned(YearMonth.of(2018, 1))
            .build()

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun `Do not reprocess behov`() {

        val behov = SubsumsjonsBehov.Builder()
            .minsteinntektResultat(
                MinsteinntektResultat(
                    "123",
                    "987",
                    "555",
                    false))
            .inntekt(Inntekt("123", emptyList()))
            .senesteInntektsmåned(YearMonth.of(2018, 1))
            .build()

        assertFalse(shouldBeProcessed(behov))
    }
}