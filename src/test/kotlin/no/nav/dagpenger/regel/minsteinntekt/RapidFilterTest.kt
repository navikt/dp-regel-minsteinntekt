package no.nav.dagpenger.regel.minsteinntekt

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.MINSTEINNTEKT_RESULTAT
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

class RapidFilterTest {
    private val testRapid = TestRapid()
    private val testMessage =
        mapOf(
            BEREGNINGSDATO to "verdi",
            INNTEKT to "verid",
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
            testMessage.muterOgKonverterToJsonString { it.remove(BEREGNINGSDATO) },
        )
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it.remove(INNTEKT) },
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

    private class TestListener(rapidsConnection: RapidsConnection) : River.PacketListener {
        var onPacketCalled = false

        init {
            River(rapidsConnection).apply(
                MinsteinntektBehovløser.rapidFilter,
            ).register(this)
        }

        override fun onPacket(
            packet: JsonMessage,
            context: MessageContext,
        ) {
            this.onPacketCalled = true
        }

        override fun onError(
            problems: MessageProblems,
            context: MessageContext,
        ) {
        }
    }
}