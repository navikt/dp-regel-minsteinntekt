package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.MINSTEINNTEKT_RESULTAT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.YearMonth

internal class KoronaBeregningTest {
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
        "2020-03-19, ORDINAER",
        "2020-03-20, KORONA",
        "2020-10-31, KORONA",
        "2020-11-01, ORDINAER",
        "2021-02-18, ORDINAER",
        "2021-02-19, KORONA",
        "2021-09-30, KORONA",
        "2021-10-01, ORDINAER",
        "2021-12-21, ORDINAER",
        "2021-12-22, KORONA",
        "2022-02-28, KORONA",
        "2022-03-01, ORDINAER",
    )
    fun Koronaperiode(beregningsdato: String, regel: String) {
        val minsteinntekt = Application(configuration)
        val json =
            """
            {
                "harAvtjentVerneplikt": false,
                "oppfyllerKravTilFangstOgFisk": false,
                "beregningsDato": $beregningsdato
            }
            """.trimIndent()
        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(testInntekt)!!)
        val outPacket = minsteinntekt.onPacket(packet)
        assertEquals(
            Beregningsregel.valueOf(regel),
            outPacket.getMapValue(MINSTEINNTEKT_RESULTAT)[MinsteinntektSubsumsjon.BEREGNINGSREGEL]
        )
    }
}
