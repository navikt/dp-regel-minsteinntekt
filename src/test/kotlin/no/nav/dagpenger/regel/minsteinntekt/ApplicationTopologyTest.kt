package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.MINSTEINNTEKT_INNTEKTSPERIODER
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.MINSTEINNTEKT_RESULTAT
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.jsonAdapterInntektPeriodeInfo
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.net.URI
import java.time.YearMonth
import java.util.Properties

internal class ApplicationTopologyTest {
    private val configuration = Configuration()

    companion object {
        val config = Properties().apply {
            this[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
            this[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
        }

        val jsonAdapterInntekt = moshiInstance.adapter(Inntekt::class.java)
    }

    @Test
    fun ` dagpengebehov without inntekt should not be processed`() {
        val minsteinntekt = Application(configuration)

        val json =
            """
            {
                "beregningsDato": "2019-05-20"
            }
            """.trimIndent()

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.regelInputTopic().also { it.pipeInput(Packet(json)) }
            assertTrue { topologyTestDriver.regelOutputTopic().isEmpty }
        }
    }

    @Test
    fun ` dagpengebehov without beregningsDato should not be processed`() {
        val minsteinntekt = Application(configuration)

        val inntekt: Inntekt = Inntekt(
            inntektsId = "12345",
            inntektsListe = listOf(
                KlassifisertInntektMåned(
                    årMåned = YearMonth.of(2018, 2),
                    klassifiserteInntekter = listOf(
                        KlassifisertInntekt(
                            beløp = BigDecimal(25000),
                            inntektKlasse = InntektKlasse.ARBEIDSINNTEKT,
                        ),
                    ),

                ),
            ),
            sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 2),
        )

        val emptyjsonBehov =
            """
            {}
            """.trimIndent()

        val packet = Packet(emptyjsonBehov)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(inntekt)!!)

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.regelInputTopic().also { it.pipeInput(packet) }
            assertTrue { topologyTestDriver.regelOutputTopic().isEmpty }
        }
    }

    @Test
    fun ` should add minsteinntektsubsumsjon`() {
        val minsteinntekt = Application(configuration)

        val inntekt: Inntekt = Inntekt(
            inntektsId = "12345",
            inntektsListe = listOf(
                KlassifisertInntektMåned(
                    årMåned = YearMonth.of(2018, 2),
                    klassifiserteInntekter = listOf(
                        KlassifisertInntekt(
                            beløp = BigDecimal(25000),
                            inntektKlasse = InntektKlasse.ARBEIDSINNTEKT,
                        ),
                    ),

                ),
            ),
            sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 2),
        )

        val json =
            """
        {
            "harAvtjentVerneplikt": true,
            "oppfyllerKravTilFangstOgFisk": false,
            "beregningsDato": "2018-03-10"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(inntekt)!!)
        packet.putValue(
            "bruktInntektsPeriode",
            mapOf(
                "førsteMåned" to YearMonth.now().toString(),
                "sisteMåned" to YearMonth.now().toString(),
            ),
        )

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.regelInputTopic().also { it.pipeInput(packet) }
            val ut = topologyTestDriver.regelOutputTopic().readValue()
            assertTrue { ut.hasField(MINSTEINNTEKT_RESULTAT) }
            assertEquals(
                "Minsteinntekt.v1",
                ut.getMapValue(MINSTEINNTEKT_RESULTAT)[MinsteinntektSubsumsjon.REGELIDENTIFIKATOR],
            )

            // test inntektsperioder are added to packet correctly
            val inntektsPerioder = ut.getNullableObjectValue(
                MINSTEINNTEKT_INNTEKTSPERIODER,
                jsonAdapterInntektPeriodeInfo::fromJsonValue,
            ) as List<InntektPeriodeInfo>
            assertEquals(3, inntektsPerioder.size)
            assertEquals(YearMonth.of(2018, 2), inntektsPerioder.find { it.periode == 1 }?.inntektsPeriode?.sisteMåned)
            assertEquals(BigDecimal(25000), inntektsPerioder.find { it.periode == 1 }?.inntekt)
        }
    }

    @Test
    fun ` should add minsteinntektsubsumsjon oppfyllerKravTilFangstOgFisk`() {
        val minsteinntekt = Application(configuration)

        val inntekt: Inntekt = Inntekt(
            inntektsId = "12345",
            inntektsListe = listOf(
                KlassifisertInntektMåned(
                    årMåned = YearMonth.of(2018, 2),
                    klassifiserteInntekter = listOf(
                        KlassifisertInntekt(
                            beløp = BigDecimal(25000),
                            inntektKlasse = InntektKlasse.ARBEIDSINNTEKT,
                        ),
                        KlassifisertInntekt(
                            beløp = BigDecimal(1000),
                            inntektKlasse = InntektKlasse.FANGST_FISKE,
                        ),
                    ),
                ),
            ),
            sisteAvsluttendeKalenderMåned = YearMonth.of(2018, 3),
        )

        val json =
            """
        {
            "harAvtjentVerneplikt": true,
            "oppfyllerKravTilFangstOgFisk": true,
            "beregningsDato": "2018-04-10"
        }
            """.trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(inntekt)!!)
        packet.putValue(
            "bruktInntektsPeriode",
            mapOf(
                "førsteMåned" to YearMonth.now().toString(),
                "sisteMåned" to YearMonth.now().toString(),
            ),
        )

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.regelInputTopic().also { it.pipeInput(packet) }
            val ut = topologyTestDriver.regelOutputTopic().readValue()
            // test inntektsperioder are added to packet correctly
            val inntektsPerioder = ut.getNullableObjectValue(
                MINSTEINNTEKT_INNTEKTSPERIODER,
                jsonAdapterInntektPeriodeInfo::fromJsonValue,
            ) as List<InntektPeriodeInfo>
            assertEquals(3, inntektsPerioder.size)
            assertEquals(YearMonth.of(2018, 3), inntektsPerioder.find { it.periode == 1 }?.inntektsPeriode?.sisteMåned)
            assertEquals(BigDecimal(26000), inntektsPerioder.find { it.periode == 1 }?.inntekt)
        }
    }

    @Test
    fun ` should add problem on failure`() {
        val minsteinntekt = Application(configuration)

        val json =
            """
            {
                "beregningsDato": "2019-05-20"
            }
            """.trimIndent()

        val inntekt =
            Inntekt(inntektsId = "12345", inntektsListe = emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.now())

        val packet = Packet(json)
        packet.putValue("oppfyllerKravTilFangstOgFisk", "ERROR")
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(inntekt)!!)

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.regelInputTopic().also { it.pipeInput(packet) }
            val ut = topologyTestDriver.regelOutputTopic().readValue()
            assert(ut.hasProblem())
            assertEquals(URI("urn:dp:error:regel"), ut.getProblem()!!.type)
            assertEquals(URI("urn:dp:regel:minsteinntekt"), ut.getProblem()!!.instance)
        }
    }
}

private fun TopologyTestDriver.regelInputTopic(): TestInputTopic<String, Packet> =
    this.createInputTopic(
        REGEL_TOPIC.name,
        REGEL_TOPIC.keySerde.serializer(),
        REGEL_TOPIC.valueSerde.serializer(),
    )

private fun TopologyTestDriver.regelOutputTopic(): TestOutputTopic<String, Packet> =
    this.createOutputTopic(
        REGEL_TOPIC.name,
        REGEL_TOPIC.keySerde.deserializer(),
        REGEL_TOPIC.valueSerde.deserializer(),
    )
