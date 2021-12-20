package no.nav.dagpenger.regel.minsteinntekt.KoronaLærling

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Application
import no.nav.dagpenger.regel.minsteinntekt.Configuration
import no.nav.dagpenger.regel.minsteinntekt.moshiInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.YearMonth

class KoronaLærlingBeregningTest {

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

    @ParameterizedTest
    @CsvSource(
        "false, 2020-03-19",
        "true, 2020-03-20",
        "true, 2020-11-20",
        "true, 2021-09-30",
        "false, 2021-10-01",
        "false, 2021-12-14",
        "true, 2021-12-15",
        "true, 2022-02-28",
        "false, 2022-03-01",
    )
    fun `Skal evaluere minsteinntekt for lærlingeperiode`(oppfyllerMinstearbeidsinntekt: Boolean, beregningsdato: String) {
        val minsteinntekt = Application(configuration)
        val json =
            """
                        {
                            "lærling": true,
                            "harAvtjentVerneplikt": false,
                            "oppfyllerKravTilFangstOgFisk": false,
                            "beregningsDato": "$beregningsdato"
                        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(testInntekt)!!)
        val outPacket = minsteinntekt.onPacket(packet)
        val evaluering = outPacket.getMapValue("minsteinntektResultat")
        assertEquals(oppfyllerMinstearbeidsinntekt, evaluering["oppfyllerMinsteinntekt"] as Boolean)
    }
}
