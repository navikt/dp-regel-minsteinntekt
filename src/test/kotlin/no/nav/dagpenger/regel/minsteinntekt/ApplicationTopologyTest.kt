package no.nav.dagpenger.regel.minsteinntekt

import io.mockk.mockk
import java.math.BigDecimal
import java.net.URI
import java.time.YearMonth
import java.util.Properties
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.MINSTEINNTEKT_INNTEKTSPERIODER
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.MINSTEINNTEKT_NARE_EVALUERING
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.MINSTEINNTEKT_RESULTAT
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.jsonAdapterInntektPeriodeInfo
import no.nav.dagpenger.streams.Topics.DAGPENGER_BEHOV_PACKET_EVENT
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ApplicationTopologyTest {
    private val configuration = Configuration()

    companion object {

        val factory = ConsumerRecordFactory<String, Packet>(
            DAGPENGER_BEHOV_PACKET_EVENT.name,
            DAGPENGER_BEHOV_PACKET_EVENT.keySerde.serializer(),
            DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.serializer()
        )

        val config = Properties().apply {
            this[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
            this[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
        }

        val jsonAdapterInntekt = moshiInstance.adapter(Inntekt::class.java)
    }

    @Test
    fun ` dagpengebehov without inntekt should not be processed`() {
        val minsteinntekt = Application(configuration, mockk(relaxed = true))

        val json = """
            {
                "beregningsDato": "2019-05-20"
            }
            """.trimIndent()

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(Packet(json))
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            assertTrue { null == ut }
        }
    }

    @Test
    fun ` dagpengebehov without beregningsDato should not be processed`() {
        val minsteinntekt = Application(configuration, mockk(relaxed = true))

        val inntekt: Inntekt = Inntekt(
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

        val emptyjsonBehov = """
            {}
            """.trimIndent()

        val packet = Packet(emptyjsonBehov)
        packet.putValue("inntektV1", jsonAdapterInntekt.toJsonValue(inntekt)!!)

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            assertTrue { null == ut }
        }
    }

    @Test
    fun ` should add minsteinntektsubsumsjon`() {
        val minsteinntekt = Application(configuration, mockk(relaxed = true))

        val inntekt: Inntekt = Inntekt(
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
            "sisteMåned" to YearMonth.now().toString()
        )
        )

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            assertTrue { ut.value().hasField(MINSTEINNTEKT_RESULTAT) }
            assertEquals(
                "Minsteinntekt.v1",
                ut.value().getMapValue(MINSTEINNTEKT_RESULTAT)[MinsteinntektSubsumsjon.REGELIDENTIFIKATOR]
            )

            // test inntektsperioder are added to packet correctly
            val inntektsPerioder = ut.value().getNullableObjectValue(
                MINSTEINNTEKT_INNTEKTSPERIODER,
                jsonAdapterInntektPeriodeInfo::fromJsonValue
            ) as List<InntektPeriodeInfo>
            assertEquals(3, inntektsPerioder.size)
            assertEquals(YearMonth.of(2018, 2), inntektsPerioder.find { it.periode == 1 }?.inntektsPeriode?.sisteMåned)
            assertEquals(BigDecimal(25000), inntektsPerioder.find { it.periode == 1 }?.inntekt)
        }
    }

    @Test
    fun ` should add minsteinntektsubsumsjon oppfyllerKravTilFangstOgFisk`() {
        val minsteinntekt = Application(configuration, mockk(relaxed = true))

        val inntekt: Inntekt = Inntekt(
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
            "sisteMåned" to YearMonth.now().toString()
        )
        )

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            // test inntektsperioder are added to packet correctly
            val inntektsPerioder = ut.value().getNullableObjectValue(
                MINSTEINNTEKT_INNTEKTSPERIODER,
                jsonAdapterInntektPeriodeInfo::fromJsonValue
            ) as List<InntektPeriodeInfo>
            assertEquals(3, inntektsPerioder.size)
            assertEquals(YearMonth.of(2018, 3), inntektsPerioder.find { it.periode == 1 }?.inntektsPeriode?.sisteMåned)
            assertEquals(BigDecimal(26000), inntektsPerioder.find { it.periode == 1 }?.inntekt)
        }
    }

    @Test
    fun ` should add nare evaluation`() {
        val minsteinntekt = Application(configuration, mockk(relaxed = true))

        val inntekt: Inntekt = Inntekt(
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
            "sisteMåned" to YearMonth.now().toString()
        )
        )

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            val nareEvaluering = Minsteinntekt.jsonAdapterEvaluering.fromJson(ut.value().getStringValue(
                MINSTEINNTEKT_NARE_EVALUERING
            ))

            val expectedNareEvaluering = kravTilMinsteinntekt.evaluer(packetToFakta(packet))

            assertEquals(expectedNareEvaluering, nareEvaluering)
        }
    }

    @Test
    fun ` should add problem on failure`() {
        val minsteinntekt = Application(configuration, mockk(relaxed = true))

        val json = """
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
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            assert(ut.value().hasProblem())
            assertEquals(URI("urn:dp:error:regel"), ut.value().getProblem()!!.type)
            assertEquals(URI("urn:dp:regel:minsteinntekt"), ut.value().getProblem()!!.instance)
        }
    }
}
