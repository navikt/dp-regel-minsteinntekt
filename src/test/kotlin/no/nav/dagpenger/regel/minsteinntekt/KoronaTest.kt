package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth

class KoronaTest {
    private val configuration = Configuration()

    val jsonAdapterInntekt = moshiInstance.adapter(Inntekt::class.java)

    val testInntekt: Inntekt = Inntekt(
        inntektsId = "12345",
        inntektsListe = listOf(
            KlassifisertInntektMåned(
                årMåned = YearMonth.of(2020, 2),
                klassifiserteInntekter = listOf(
                    KlassifisertInntekt(
                        beløp = BigDecimal(98866),
                        inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        ),
        sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 2)
    )

    @Test
    fun `Skal innvilge over 0,75G siste 12 måneder når korona-toggle er på`() {
        val minsteinntekt = Minsteinntekt(configuration)

        val json = """
        {
            "harAvtjentVerneplikt": false,
            "oppfyllerKravTilFangstOgFisk": false,
            "beregningsDato": "2018-03-10",
            "feature-flag-korona": "true"
        }""".trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(testInntekt)!!)

        val outPacket = minsteinntekt.onPacket(packet)

        assertTrue(outPacket.getMapValue("minsteinntektResultat")["oppfyllerMinsteinntekt"] as Boolean)
    }
}