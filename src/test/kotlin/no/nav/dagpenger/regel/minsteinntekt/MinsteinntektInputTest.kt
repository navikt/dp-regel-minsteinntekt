package no.nav.dagpenger.regel.minsteinntekt

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
            .inntekt(0)
            .build()

        assert(shouldBeProcessed(behov))
    }

    @Test
    fun `Do not reprocess behov with subsumsjonsId`() {

        val behov = SubsumsjonsBehov.Builder()
            .minsteinntektSubsumsjon(SubsumsjonsBehov.MinsteinntektSubsumsjon("123", "987", "555", false))
            .build()

        assertFalse(shouldBeProcessed(behov))
    }
}