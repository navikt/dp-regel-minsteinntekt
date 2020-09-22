package no.nav.dagpenger.regel.minsteinntekt

import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class PacketToFaktaTest {

    val emptyInntekt: Inntekt = Inntekt(
        inntektsId = "12345",
        inntektsListe = emptyList(),
        sisteAvsluttendeKalenderMåned = YearMonth.now()
    )

    @Test
    fun ` should map fangst_og_fisk from packet to Fakta `() {
        val json =
            """
        {
            "oppfyllerKravTilFangstOgFisk": true,
            "beregningsDato": "2019-04-10"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", ApplicationTopologyTest.jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertTrue(fakta.fangstOgFisk)
    }

    @Test
    fun ` should map beregningsdato from packet to Fakta `() {
        val json =
            """
        {
            "oppfyllerKravTilFangstOgFisk": true,
            "beregningsDato": "2019-04-10"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", ApplicationTopologyTest.jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertEquals(LocalDate.of(2019, 4, 10), fakta.beregningsdato)
    }

    @Test
    fun ` should have the right grunnbeløp `() {
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

    @Test
    fun ` should map avtjent_verneplikt from packet to Fakta `() {
        val json =
            """
        {
            "harAvtjentVerneplikt": true,
            "beregningsDato": "2019-04-10"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", ApplicationTopologyTest.jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertTrue(fakta.verneplikt)
    }

    @Test
    fun ` should map brukt_inntektsperiode from packet to Fakta `() {
        val json =
            """
        {
            "bruktInntektsPeriode": {"førsteMåned":"2019-02", "sisteMåned":"2019-03"},
            "beregningsDato": "2019-04-10"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", ApplicationTopologyTest.jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertEquals(YearMonth.of(2019, 2), fakta.bruktInntektsPeriode!!.førsteMåned)
        assertEquals(YearMonth.of(2019, 3), fakta.bruktInntektsPeriode!!.sisteMåned)
    }

    @Test
    fun ` should map inntekt from packet to Fakta `() {
        val json =
            """
        {
            "beregningsDato": "2019-04-10"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", ApplicationTopologyTest.jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertEquals("12345", fakta.inntekt.inntektsId)
    }

    @Test
    fun ` should map lærling from packet to Fakta `() {
        val json =
            """
        {
            "lærling": true,
            "beregningsDato": "2019-04-10"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", ApplicationTopologyTest.jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertTrue(fakta.lærling)
    }
}

class KoronaRegelverkPeriodeTest : FreeSpec({
    "sjekke grenseverdiene for koronaperiode " {
        io.kotest.data.forAll(
            table(
                headers("dato", "er korona periode ?"),
                row(LocalDate.of(2020, 3, 19), false),
                row(LocalDate.of(2020, 3, 20), true),
                row(LocalDate.of(2020, 10, 31), true),
                row(LocalDate.of(2020, 11, 1), false),
            )
        ) { dato: LocalDate, erKoronaPeriode: Boolean ->
            dato.erKoronaPeriode() shouldBe erKoronaPeriode
        }
    }
})
