package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.streams.Topics
import no.nav.dagpenger.streams.Topics.DAGPENGER_BEHOV_PACKET_EVENT
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth
import java.util.Properties

class MinsteinntektTopologyTest {

    companion object {

        val factory = ConsumerRecordFactory<String, Packet>(
            Topics.DAGPENGER_BEHOV_PACKET_EVENT.name,
            Topics.DAGPENGER_BEHOV_PACKET_EVENT.keySerde.serializer(),
            Topics.DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.serializer()
        )

        val config = Properties().apply {
            this[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
            this[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
        }

        val jsonAdapterInntekt = moshiInstance.adapter(Inntekt::class.java)
        val jsonAdapterInntektsPeriode = moshiInstance.adapter(InntektsPeriode::class.java)
    }

    @Test
    fun ` dagpengebehov without inntekt and senesteinntektsmåned should not be processed`() {
        val minsteinntekt = Minsteinntekt(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )

        val emptyjsonBehov = """
            {}
            """.trimIndent()

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(Packet(emptyjsonBehov))
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
        val minsteinntekt = Minsteinntekt(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )

        val inntekt: Inntekt = Inntekt(
            inntektsId = "12345",
            inntektsListe = listOf(
                KlassifisertInntektMåned(
                    årMåned = YearMonth.of(2019, 2),
                    klassifiserteInntekter = listOf(
                        KlassifisertInntekt(
                            beløp = BigDecimal(25000),
                            inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                        )
                    )

                )
            )
        )

        val json = """
        {
            "senesteInntektsmåned":"2018-03",
            "harAvtjentVerneplikt": true,
            "fangstOgFisk": false
        }""".trimIndent()

        val packet = Packet(json)
        packet.putValue("inntektV1", inntekt) { jsonAdapterInntekt.toJson(it) }
        packet.putValue(
            "bruktInntektsPeriode",
            InntektsPeriode(YearMonth.now(), YearMonth.now())
        ) { jsonAdapterInntektsPeriode.toJson(it) }

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            assertTrue { ut.value().hasField(Minsteinntekt.MINSTEINNTEKT_RESULTAT) }
            assertEquals("Minsteinntekt.v1", ut.value().getMapValue(Minsteinntekt.MINSTEINNTEKT_RESULTAT)[MinsteinntektSubsumsjon.REGELIDENTIFIKATOR])
        }
    }
}