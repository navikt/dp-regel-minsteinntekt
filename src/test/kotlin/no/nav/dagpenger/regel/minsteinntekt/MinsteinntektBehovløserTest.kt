package no.nav.dagpenger.regel.minsteinntekt

import io.kotest.matchers.shouldBe
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

class MinsteinntektBehovløserTest {

    private val testRapid = TestRapid()

    init {
        MinsteinntektBehovløser(testRapid)
    }

    @Test
    fun tull() {
        0 shouldBe 0
    }
}
