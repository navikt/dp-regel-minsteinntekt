package no.nav.dagpenger.regel.minsteinntekt

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MinsteinntektInputTest {

    @Test
    fun `Process behov without inntekt and no inntekt tasks`() {
        val behov = SubsumsjonsBehov.Builder()
                .vedtaksId("9988")
                .aktorId("1233")
                .beregningsDato(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .build()

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun `Process behov without inntekt and no hentInntekt task`() {
        val behov = SubsumsjonsBehov.Builder()
                .vedtaksId("9988")
                .aktorId("1233")
                .beregningsDato(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .task(listOf("noe annet"))
            .build()

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun `Do not process behov without inntekt but with hentInntekt task`() {
        val behov = SubsumsjonsBehov.Builder()
            .vedtaksId("123456")
            .aktorId("123")
            .beregningsDato(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .task(listOf("hentInntekt"))
            .build()
        assertFalse(shouldBeProcessed(behov))
    }

    @Test
    fun `Do not reprocess behov whit subsumsjonsId`() {

        val behov = SubsumsjonsBehov.Builder()
            .aktorId("123")
            .inntekt(0)
            .minsteinntektSubsumsjon(SubsumsjonsBehov.MinsteinntektSubsumsjon("123", "987", "555", false))
            .build()

        assertFalse(shouldBeProcessed(behov))
    }
}