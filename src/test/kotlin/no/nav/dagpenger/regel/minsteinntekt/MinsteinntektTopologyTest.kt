package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.net.URI
import java.time.YearMonth
import java.util.function.Predicate

class MinsteinntektTopologyTest {

    private val testEnv = Environment(username = "test", password = "test")
    private val jsonAdapterInntekt = moshiInstance.adapter(Inntekt::class.java)
    private fun List<Predicate<Packet>>.shouldProcess(packet: Packet) = this.all { it.test(packet) }

    @Test
    fun `dagpengebehov without inntekt should not be processed`() {
        assertFalse(Minsteinntekt(testEnv).filterPredicates().shouldProcess(Packet().apply {
            putValue(Minsteinntekt.BEREGNINGSDAGTO, "dato")
        }))
    }

    @Test
    fun `dagpengebehov without beregningsDato should not be processed`() {
        assertFalse(Minsteinntekt(testEnv).filterPredicates().shouldProcess(Packet().apply {
            putValue(Minsteinntekt.INNTEKT, "inntekt")
        }))
    }

    @Test
    fun `dagpengebehov with beregningsDato, inntekt and  minsteinntekt result should not be processed`() {
        assertFalse(Minsteinntekt(testEnv).filterPredicates().shouldProcess(Packet().apply {
            putValue(Minsteinntekt.INNTEKT, "inntekt")
            putValue(Minsteinntekt.BEREGNINGSDAGTO, "dato")
            putValue(Minsteinntekt.MINSTEINNTEKT_RESULTAT, "resultat")
        }))
    }

    @Test
    fun `dagpengebehov with beregningsDato, inntekt and without minsteinntekt result should  be processed`() {
        assertTrue(Minsteinntekt(testEnv).filterPredicates().shouldProcess(Packet().apply {
            putValue(Minsteinntekt.INNTEKT, "inntekt")
            putValue(Minsteinntekt.BEREGNINGSDAGTO, "dato")
        }))
    }

    @Test
    fun `should add minsteinntektsubsumsjon`() {

        val inntekt = Inntekt(
            inntektsId = "12345",
            inntektsListe = listOf(
                KlassifisertInntektMåned(
                    årMåned = YearMonth.of(2018, 2),
                    klassifiserteInntekter = listOf(
                        KlassifisertInntekt(
                            beløp = BigDecimal(25000),
                            inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                        )
                    )

                )
            ),
            sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 2)
        )

        val json = """
        {
            "harAvtjentVerneplikt": true,
            "oppfyllerKravTilFangstOgFisk": false,
            "beregningsDato": "2018-03-10"
        }""".trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(inntekt)!!)
        packet.putValue(
            "bruktInntektsPeriode", mapOf(
            "førsteMåned" to YearMonth.now().toString(),
            "sisteMåned" to YearMonth.now().toString()))

        val minsteinntekt = Minsteinntekt(testEnv)
        val ut = minsteinntekt.onPacket(packet)

        assertTrue { ut.hasField(Minsteinntekt.MINSTEINNTEKT_RESULTAT) }
        assertEquals(
            "Minsteinntekt.v1",
            ut.getMapValue(Minsteinntekt.MINSTEINNTEKT_RESULTAT)[MinsteinntektSubsumsjon.REGELIDENTIFIKATOR]
        )

        // test inntektsperioder are added to packet correctly
        val inntektsPerioder = ut.getNullableObjectValue(
            Minsteinntekt.MINSTEINNTEKT_INNTEKTSPERIODER,
            minsteinntekt.jsonAdapterInntektPeriodeInfo::fromJsonValue
        ) as List<InntektPeriodeInfo>
        assertEquals(3, inntektsPerioder.size)
        assertEquals(YearMonth.of(2018, 2), inntektsPerioder.find { it.periode == 1 }?.inntektsPeriode?.sisteMåned)
        assertEquals(BigDecimal(25000), inntektsPerioder.find { it.periode == 1 }?.inntekt)
    }

    @Test
    fun ` should add minsteinntektsubsumsjon oppfyllerKravTilFangstOgFisk`() {
        val minsteinntekt = Minsteinntekt(testEnv)

        val inntekt = Inntekt(
            inntektsId = "12345",
            inntektsListe = listOf(
                KlassifisertInntektMåned(
                    årMåned = YearMonth.of(2018, 2),
                    klassifiserteInntekter = listOf(
                        KlassifisertInntekt(
                            beløp = BigDecimal(25000),
                            inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                        ),
                        KlassifisertInntekt(
                            beløp = BigDecimal(1000),
                            inntektKlasse = InntektKlasse.FANGST_FISKE
                        )
                    )
                )
            ),
            sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 3)
        )

        val json = """
        {
            "harAvtjentVerneplikt": true,
            "oppfyllerKravTilFangstOgFisk": true,
            "beregningsDato": "2018-04-10"
        }""".trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(inntekt)!!)
        packet.putValue(
            "bruktInntektsPeriode", mapOf(
            "førsteMåned" to YearMonth.now().toString(),
            "sisteMåned" to YearMonth.now().toString()))

        val ut = minsteinntekt.onPacket(packet)
        // test inntektsperioder are added to packet correctly
        val inntektsPerioder = ut.getNullableObjectValue(
            Minsteinntekt.MINSTEINNTEKT_INNTEKTSPERIODER,
            minsteinntekt.jsonAdapterInntektPeriodeInfo::fromJsonValue
        ) as List<InntektPeriodeInfo>
        assertEquals(3, inntektsPerioder.size)
        assertEquals(YearMonth.of(2018, 3), inntektsPerioder.find { it.periode == 1 }?.inntektsPeriode?.sisteMåned)
        assertEquals(BigDecimal(26000), inntektsPerioder.find { it.periode == 1 }?.inntekt)
    }

    @Test
    fun ` should add problem on failure`() {
        val ut = Minsteinntekt(testEnv).onFailure(Packet(), null)
        assertEquals(URI("urn:dp:error:regel"), ut.getProblem()!!.type)
        assertEquals(URI("urn:dp:regel:minsteinntekt"), ut.getProblem()!!.instance)
    }
}