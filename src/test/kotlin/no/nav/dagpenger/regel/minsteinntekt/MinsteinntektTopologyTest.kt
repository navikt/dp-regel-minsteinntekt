package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.streams.Topics
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Properties
import java.util.Random
import kotlin.test.assertTrue

class MinsteinntektTopologyTest {

    val jsonAdapter = moshiInstance.adapter(SubsumsjonsBehov::class.java)

    companion object {
        val factory = ConsumerRecordFactory<String, String>(
                Topics.DAGPENGER_BEHOV_EVENT.name,
                Serdes.String().serializer(),
                Serdes.String().serializer()
        )

        val config = Properties().apply {
            this[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
            this[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
        }
    }

    @Test
    fun ` Should add inntekt task to subsumsjonsBehov without inntekt `() {
        val datalaster = MinsteinntektRegel(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )

        val behov = SubsumsjonsBehov(
            "34512",
            "12345",
            Random().nextInt(),
            LocalDate.now())
        val behovJson = jsonAdapter.toJson(behov)

        TopologyTestDriver(datalaster.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(behovJson)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                Topics.DAGPENGER_BEHOV_EVENT.name,
                Serdes.String().deserializer(),
                Serdes.String().deserializer()
            )

            val utBehov = jsonAdapter.fromJson(ut.value())!!

            assertTrue("Inntekt task should have been added") { utBehov.tasks != null }
        }
    }

    @Test
    fun ` Should add MinsteinntektSubumsjon to subsumsjonsBehov with inntekt `() {
        val datalaster = MinsteinntektRegel(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )

        val behov = SubsumsjonsBehov(
            "34512",
            "12345",
            Random().nextInt(),
            LocalDate.now(),
            inntekt = 500000)
        val behovJson = jsonAdapter.toJson(behov)

        TopologyTestDriver(datalaster.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(behovJson)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                Topics.DAGPENGER_BEHOV_EVENT.name,
                Serdes.String().deserializer(),
                Serdes.String().deserializer()
            )

            val utBehov = jsonAdapter.fromJson(ut.value())!!

            assertTrue("MinsteinntektSubsumsjon should have been added") {
                utBehov.minsteinntektSubsumsjon != null
            }
        }
    }
}