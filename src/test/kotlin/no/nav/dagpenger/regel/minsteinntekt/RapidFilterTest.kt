package no.nav.dagpenger.regel.minsteinntekt

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.MeterRegistry
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.BEHOV_ID
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.MINSTEINNTEKT_RESULTAT
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.PROBLEM
import org.junit.jupiter.api.Test

class RapidFilterTest {
    private val testRapid = TestRapid()
    private val testMessage =
        mapOf(
            BEHOV_ID to "behovIdVerdi",
            BEREGNINGSDATO to "beregningsdatoVerdi",
            INNTEKT to "inntektVerdi",
        )

    @Test
    fun `Skal behandle pakker med alle required keys`() {
        val testListener = TestListener(testRapid)
        testRapid.sendTestMessage(
            JsonMessage.newMessage(testMessage).toJson(),
        )
        testListener.onPacketCalled shouldBe true
    }

    @Test
    fun `Skal ikke behandle pakker med manglende required keys`() {
        val testListener = TestListener(testRapid)

        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it.remove(BEHOV_ID) },
        )
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it.remove(BEREGNINGSDATO) },
        )
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it.remove(INNTEKT) },
        )
        testListener.onPacketCalled shouldBe false
    }

    @Test
    fun `Skal ikke behandle pakker med problem`() {
        val testListener = TestListener(testRapid)
        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it[PROBLEM] = "problem" },
        )
        testListener.onPacketCalled shouldBe false
    }

    @Test
    fun `Skal ikke behandle pakker med løsning`() {
        val testListener = TestListener(testRapid)
        val messageMedLøsning =
            testMessage.toMutableMap().also {
                it[MINSTEINNTEKT_RESULTAT] = "verdi"
            }
        testRapid.sendTestMessage(
            JsonMessage.newMessage(messageMedLøsning).toJson(),
        )
        testListener.onPacketCalled shouldBe false
    }

    private fun Map<String, Any>.muterOgKonverterToJsonString(block: (map: MutableMap<String, Any>) -> Unit): String {
        val mutableMap = this.toMutableMap()
        block.invoke(mutableMap)
        return JsonMessage.newMessage(mutableMap).toJson()
    }

    private class TestListener(
        rapidsConnection: RapidsConnection,
    ) : River.PacketListener {
        var onPacketCalled = false

        init {
            River(rapidsConnection)
                .apply(
                    MinsteinntektBehovløser.rapidFilter,
                ).register(this)
        }

        override fun onPacket(
            packet: JsonMessage,
            context: MessageContext,
            metadata: MessageMetadata,
            meterRegistry: MeterRegistry,
        ) {
            this.onPacketCalled = true
        }

        override fun onError(
            problems: MessageProblems,
            context: MessageContext,
            metadata: MessageMetadata,
        ) {
        }
    }
}
