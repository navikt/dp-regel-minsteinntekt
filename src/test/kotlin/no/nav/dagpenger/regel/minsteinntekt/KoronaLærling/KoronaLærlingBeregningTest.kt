package no.nav.dagpenger.regel.minsteinntekt.KoronaLærling

import com.squareup.moshi.JsonAdapter
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Application
import no.nav.dagpenger.regel.minsteinntekt.Beregningsregel
import no.nav.dagpenger.regel.minsteinntekt.Configuration
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.MINSTEINNTEKT_NARE_EVALUERING
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.MINSTEINNTEKT_RESULTAT
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektSubsumsjon
import no.nav.dagpenger.regel.minsteinntekt.moshiInstance
import no.nav.nare.core.evaluations.Evaluering
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth

class KoronaLærlingBeregningTest {
    private val configuration = Configuration()
    private val jsonAdapterEvaluering: JsonAdapter<Evaluering> = moshiInstance.adapter(Evaluering::class.java)

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
    fun `Skal bruke korona-regler når beregningsdato er etter 20 nov 2020 og søker er lærling`() {
        val minsteinntekt = Application(configuration)

        val json =
            """
        {
            "lærling": true,
            "harAvtjentVerneplikt": false,
            "oppfyllerKravTilFangstOgFisk": false,
            "beregningsDato": "2020-11-20"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(testInntekt)!!)

        val outPacket = minsteinntekt.onPacket(packet)
        val evaluering =
            jsonAdapterEvaluering.fromJson(outPacket.getStringValue(MINSTEINNTEKT_NARE_EVALUERING))!!

        assertTrue(evaluering.children.any { it.identifikator == "Krav til minsteinntekt etter midlertidig korona-endret § 4-4" })
        assertEquals(Beregningsregel.KORONA, outPacket.getMapValue(MINSTEINNTEKT_RESULTAT)[MinsteinntektSubsumsjon.BEREGNINGSREGEL])
    }

    @Test
    fun `Skal ikke bruke korona-regler når beregningsdato er før 20 mars 2020 og søker er lærling`() {
        val minsteinntekt = Application(configuration)

        val json =
            """
        {
            "lærling": true,
            "harAvtjentVerneplikt": false,
            "oppfyllerKravTilFangstOgFisk": false,
            "beregningsDato": "2020-03-19"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(testInntekt)!!)

        val outPacket = minsteinntekt.onPacket(packet)
        val evaluering =
            jsonAdapterEvaluering.fromJson(outPacket.getStringValue(MINSTEINNTEKT_NARE_EVALUERING))!!

        assertTrue(evaluering.children.none { it.identifikator == "Krav til minsteinntekt etter midlertidig korona-endret § 4-4" })
        assertEquals(Beregningsregel.ORDINAER, outPacket.getMapValue(MINSTEINNTEKT_RESULTAT)[MinsteinntektSubsumsjon.BEREGNINGSREGEL])
    }
}
