package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.YearMonth

class PacketToFaktaTest {

    val emptyInntekt: Inntekt = Inntekt(
        inntektsId = "12345",
        inntektsListe = emptyList()
    )

    @Test
    fun ` should map fangst_og_fisk from packet to Fakta `() {
        val json = """
        {
            "senesteInntektsmåned":"2018-03",
            "oppfyllerKravTilFangstOgFisk": true
        }""".trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", MinsteinntektTopologyTest.jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertTrue(fakta.fangstOgFisk)
    }

    @Test
    fun ` should map avtjent_verneplikt from packet to Fakta `() {
        val json = """
        {
            "senesteInntektsmåned":"2018-03",
            "harAvtjentVerneplikt": true
        }""".trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", MinsteinntektTopologyTest.jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertTrue(fakta.verneplikt)
    }

    @Test
    fun ` should map brukt_inntektsperiode from packet to Fakta `() {
        val json = """
        {
            "senesteInntektsmåned":"2018-03",
            "bruktInntektsPeriode": {"førsteMåned":"2019-02", "sisteMåned":"2019-03"}
        }""".trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", MinsteinntektTopologyTest.jsonAdapterInntekt.toJsonValue(emptyInntekt)!!)

        val fakta = packetToFakta(packet)

        assertEquals(YearMonth.of(2019, 2), fakta.bruktInntektsPeriode!!.førsteMåned)
        assertEquals(YearMonth.of(2019, 3), fakta.bruktInntektsPeriode!!.sisteMåned)
    }
}