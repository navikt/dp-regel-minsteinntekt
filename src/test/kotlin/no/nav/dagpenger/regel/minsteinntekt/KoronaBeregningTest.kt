package no.nav.dagpenger.regel.minsteinntekt

import com.squareup.moshi.JsonAdapter
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.MINSTEINNTEKT_RESULTAT
import no.nav.nare.core.evaluations.Evaluering
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth

internal class KoronaBeregningTest {
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

    fun withKoronaperiode(test: () -> Unit) {
        try {
            System.setProperty("feature.koronaperiode2", "true")
            test()
        } finally {
            System.clearProperty("feature.koronaperiode2")
        }
    }
    fun withoutKoronaperiode(test: () -> Unit) {
        try {
            System.setProperty("feature.koronaperiode2", "false")
            test()
        } finally {
            System.clearProperty("feature.koronaperiode2")
        }
    }

    @Test
    fun `Skal bruke korona-regler når beregningsdato er etter 20 mars 2020 men før 31 oktober 2020`() {
        val minsteinntekt = Application(configuration)

        val json =
            """
        {
            "harAvtjentVerneplikt": false,
            "oppfyllerKravTilFangstOgFisk": false,
            "beregningsDato": "2020-03-20"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(testInntekt)!!)

        val outPacket = minsteinntekt.onPacket(packet)
        assertEquals(Beregningsregel.KORONA, outPacket.getMapValue(MINSTEINNTEKT_RESULTAT)[MinsteinntektSubsumsjon.BEREGNINGSREGEL])
    }

    @Test
    fun `Skal ikke bruke korona-regler når beregningsdato er før 20 mars 2020`() {
        val minsteinntekt = Application(configuration)

        val json =
            """
        {
            "harAvtjentVerneplikt": false,
            "oppfyllerKravTilFangstOgFisk": false,
            "beregningsDato": "2020-03-19"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(testInntekt)!!)

        val outPacket = minsteinntekt.onPacket(packet)
        assertEquals(Beregningsregel.ORDINAER, outPacket.getMapValue(MINSTEINNTEKT_RESULTAT)[MinsteinntektSubsumsjon.BEREGNINGSREGEL])
    }

    @Test
    fun `Skal ikke bruke korona-regler når koronatoggle er av`() {
        val minsteinntekt = Application(configuration)

        val json =
            """
        {
            "harAvtjentVerneplikt": false,
            "oppfyllerKravTilFangstOgFisk": false,
            "beregningsDato": "2020-03-19"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(testInntekt)!!)

        val outPacket = minsteinntekt.onPacket(packet)
        assertEquals(Beregningsregel.ORDINAER, outPacket.getMapValue(MINSTEINNTEKT_RESULTAT)[MinsteinntektSubsumsjon.BEREGNINGSREGEL])
    }

    @Test
    fun `Skal bruke korona-regler når beregningsdato er etter 19 februar 2021 men før 30 juni 2021`() {
        withKoronaperiode {
            val minsteinntekt = Application(configuration)

            val json =
                """
        {
            "harAvtjentVerneplikt": false,
            "oppfyllerKravTilFangstOgFisk": false,
            "beregningsDato": "2021-02-19"
        }
                """.trimIndent()

            val packet = Packet(json)
            packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(testInntekt)!!)

            val outPacket = minsteinntekt.onPacket(packet)
            assertEquals(Beregningsregel.KORONA, outPacket.getMapValue(MINSTEINNTEKT_RESULTAT)[MinsteinntektSubsumsjon.BEREGNINGSREGEL])
        }
    }
    @Test
    fun `Skal ikke bruke korona-regler når beregningsdato er etter 1 februar 2021 men før 30 juni 2021 men flagget ikke er satt`() {
        withoutKoronaperiode {
            val minsteinntekt = Application(configuration)

            val json =
                """
        {
            "harAvtjentVerneplikt": false,
            "oppfyllerKravTilFangstOgFisk": false,
            "beregningsDato": "2021-02-01"
        }
                """.trimIndent()

            val packet = Packet(json)
            packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(testInntekt)!!)

            val outPacket = minsteinntekt.onPacket(packet)
            assertEquals(Beregningsregel.ORDINAER, outPacket.getMapValue(MINSTEINNTEKT_RESULTAT)[MinsteinntektSubsumsjon.BEREGNINGSREGEL])
        }
    }
}
