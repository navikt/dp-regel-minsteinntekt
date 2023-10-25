package no.nav.dagpenger.regel.minsteinntekt

import io.getunleash.FakeUnleash
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.YearMonth

class PacketToFaktaGjusteringTest {
    private val emptyInntekt: Inntekt = Inntekt(
        inntektsId = "12345",
        inntektsListe = emptyList(),
        sisteAvsluttendeKalenderMåned = YearMonth.now(),
    )

    @Test
    fun ` should have the grunnbeløp without "gjustering" when beregningsdato is before justering date and featureflag is on `() {
        val unleash = FakeUnleash().also { it.enable(GJUSTERING_TEST) }

        val json =
            """
        {
            "oppfyllerKravTilFangstOgFisk": true,
            "beregningsDato": "2019-05-30"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", ApplicationTopologyTest.jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet, GrunnbeløpStrategy(unleash))

        assertEquals(99858.toBigDecimal(), fakta.grunnbeløp)
    }

    @Test
    fun ` should have grunnbeløp with "gjustering" when beregningsdato is after justering date and featureflag is on`() {
        val unleash = FakeUnleash().also { it.enable(GJUSTERING_TEST) }

        val adapter = moshiInstance.adapter(Inntekt::class.java)

        val json =
            """
        {
            "oppfyllerKravTilFangstOgFisk": true,
            "beregningsDato": "2023-05-15"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", adapter.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet, GrunnbeløpStrategy(unleash))

        assertEquals(117000.toBigDecimal(), fakta.grunnbeløp)
    }

    @Test
    fun ` should have grunnbeløp without "gjustering" when beregningsdato is after justering date and featureflag is off`() {
        val adapter = moshiInstance.adapter(Inntekt::class.java)

        val json =
            """
        {
            "oppfyllerKravTilFangstOgFisk": true,
            "beregningsDato": "2020-09-02"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", adapter.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet, GrunnbeløpStrategy(FakeUnleash().apply { this.disableAll() }))

        assertEquals(99858.toBigDecimal(), fakta.grunnbeløp)
    }
}
