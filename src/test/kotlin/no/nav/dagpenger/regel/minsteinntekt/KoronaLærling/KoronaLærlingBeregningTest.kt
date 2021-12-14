package no.nav.dagpenger.regel.minsteinntekt.KoronaLærling

import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.row
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Application
import no.nav.dagpenger.regel.minsteinntekt.Configuration
import no.nav.dagpenger.regel.minsteinntekt.moshiInstance
import org.junit.jupiter.api.Assertions.assertEquals
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class KoronaLærlingBeregningTest : FreeSpec(
    {
        val minsteinntekt = Application(configuration)
        "skal finne evaluere minsteinntekt ved lærling forskrift" - {
            listOf(
                row(false, LocalDate.of(2020, 3, 19)),
                row(true, LocalDate.of(2020, 3, 21)),
                row(true, LocalDate.of(2020, 11, 20)),
                row(true, LocalDate.of(2021, 9, 30)),
                row(false, LocalDate.of(2021, 10, 1)),
                row(false, LocalDate.of(2021, 12, 21)),
                row(true, LocalDate.of(2021, 12, 22)),
                row(true, LocalDate.of(2022, 2, 28)),
                row(false, LocalDate.of(2022, 3, 1)),

            ).map { (oppfyllerMinstearbeidsinntekt: Boolean, beregningsdato: LocalDate) ->

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
    }

)

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
