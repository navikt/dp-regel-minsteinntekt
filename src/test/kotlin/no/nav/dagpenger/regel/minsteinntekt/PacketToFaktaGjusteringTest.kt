package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.YearMonth

class PacketToFaktaGjusteringTest {

    fun withGjustering(test: () -> Unit) {
        try {
            System.setProperty("feature.gjustering", "true")
            test()
        } finally {
            System.clearProperty("feature.gjustering")
        }
    }

    val emptyInntekt: Inntekt = Inntekt(
        inntektsId = "12345",
        inntektsListe = emptyList(),
        sisteAvsluttendeKalenderMåned = YearMonth.now()
    )

    @Test
    fun ` should have the grunnbeløp without "gjustering" when beregningsdato is before justering date and featureflag is on `() {
        withGjustering {
            val json =
                """
        {
            "oppfyllerKravTilFangstOgFisk": true,
            "beregningsDato": "2019-05-30"
        }
                """.trimIndent()

            val packet = Packet(json)
            packet.putValue("inntektV1", ApplicationTopologyTest.jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

            val fakta = packetToFakta(packet)

            assertEquals(99858.toBigDecimal(), fakta.grunnbeløp)
        }
    }

    @Test
    fun ` should have grunnbeløp with "gjustering" when beregningsdato is after justering date and featureflag is on`() {
        withGjustering {
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

            val fakta = packetToFakta(packet)

            assertEquals(101351.toBigDecimal(), fakta.grunnbeløp)
        }
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

        val fakta = packetToFakta(packet)

        assertEquals(99858.toBigDecimal(), fakta.grunnbeløp)
    }
}
