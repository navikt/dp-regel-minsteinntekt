package no.nav.dagpenger.regel.minsteinntekt

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import org.junit.jupiter.api.Test

class FaktaMapperTest {

    @Test
    fun tøys() {
    }

    private companion object {
    }
}

private class OnPacketTestListener(rapidsConnection: RapidsConnection) : River.PacketListener {
    var problems: MessageProblems? = null
    lateinit var packet: JsonMessage

    init {
        River(rapidsConnection).apply(MinsteinntektBehovløser.rapidFilter).register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        this.packet = packet
    }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
    ) {
        this.problems = problems
    }
}
