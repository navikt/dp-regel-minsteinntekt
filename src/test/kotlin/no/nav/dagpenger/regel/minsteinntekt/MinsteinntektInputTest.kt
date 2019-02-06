package no.nav.dagpenger.regel.minsteinntekt

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MinsteinntektInputTest {

    @Test
    fun `Process behov without inntekt and no inntekt tasks`() {
        val behov = SubsumsjonsBehov(
            behovId = "123456",
            aktørId = "123456",
            vedtakId = 123,
            beregningsDato = LocalDate.now()
        )

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun `Process behov with inntekt`() {
        val behov = SubsumsjonsBehov(
            behovId = "123456",
            aktørId = "123456",
            vedtakId = 123,
            beregningsDato = LocalDate.now(),
            inntekt = 555
        )

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun `Do not reprocess behov`() {
        val behovWithTask = SubsumsjonsBehov(
            behovId = "123456",
            aktørId = "123456",
            vedtakId = 123,
            beregningsDato = LocalDate.now(),
            tasks = listOf("minsteinntekt")
        )

        val behovWithSubsumsjon = SubsumsjonsBehov(
            behovId = "123456",
            aktørId = "123456",
            vedtakId = 123,
            beregningsDato = LocalDate.now(),
            inntekt = 555,
            minsteinntektSubsumsjon = MinsteinntektSubsumsjon(
                "123",
                "456",
                "789",
                false)
        )

        assertFalse(shouldBeProcessed(behovWithTask))
        assertFalse(shouldBeProcessed(behovWithSubsumsjon))
    }
}