package no.nav.dagpenger.regel.minsteinntekt

import io.getunleash.FakeUnleash
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.BEHOV_ID
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.BRUKT_INNTEKTSPERIODE
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.FANGST_OG_FISKE
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.LÆRLING
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.REGELVERKSDATO
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException

class FaktaMapperTest {
    private val testRapid = TestRapid()

    private companion object {
        private val emptyInntekt =
            mapOf(
                "inntektsId" to "12345",
                "inntektsListe" to emptyList<String>(),
                "sisteAvsluttendeKalenderMåned" to YearMonth.of(2019, 3),
            )

        private fun testMessage(
            behovId: String = "behovId",
            beregningsdato: Any = LocalDate.MAX,
            regelverksdato: Any? = LocalDate.MAX,
            inntekt: Any = emptyInntekt,
            bruktInntektsperiode: Any? = null,
            lærling: Any? = false,
            fangstOgFiske: Any? = true,
            avtjentVerneplikt: Any? = false,
        ): String {
            val testMap: MutableMap<String, Any> =
                mutableMapOf(
                    BEHOV_ID to behovId,
                    INNTEKT to inntekt,
                    BEREGNINGSDATO to beregningsdato,
                )

            fangstOgFiske?.let {
                testMap[FANGST_OG_FISKE] = it
            }

            lærling?.let {
                testMap[LÆRLING] = it
            }

            avtjentVerneplikt?.let {
                testMap[AVTJENT_VERNEPLIKT] = it
            }

            regelverksdato?.let {
                testMap[REGELVERKSDATO] = it
            }

            bruktInntektsperiode?.let {
                testMap[BRUKT_INNTEKTSPERIODE] = it
            }

//            inntekt.let {
//                testMap[INNTEKT] = it
//            }
            return JsonMessage.newMessage(testMap).toJson()
        }
    }

    @Test
    fun `Fangst og fiske blir mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage(testMessage(fangstOgFiske = true))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).fangstOgFiske shouldBe true

        testRapid.sendTestMessage(testMessage(fangstOgFiske = false))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).fangstOgFiske shouldBe false

        testRapid.sendTestMessage(testMessage(fangstOgFiske = null))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).fangstOgFiske shouldBe false

        testRapid.sendTestMessage(testMessage(fangstOgFiske = 100))
        shouldThrow<IllegalArgumentException> {
            packetToFakta(behovløser.packet, GrunnbeløpStrategy()).fangstOgFiske
        }
    }

    @Test
    fun `Avtjent verneplikt blir mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage(testMessage(avtjentVerneplikt = true))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).verneplikt shouldBe true

        testRapid.sendTestMessage(testMessage(avtjentVerneplikt = false))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).verneplikt shouldBe false

        testRapid.sendTestMessage(testMessage(avtjentVerneplikt = null))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).verneplikt shouldBe false

        testRapid.sendTestMessage(testMessage(avtjentVerneplikt = 100))
        shouldThrow<IllegalArgumentException> {
            packetToFakta(behovløser.packet, GrunnbeløpStrategy()).verneplikt
        }
    }

    @Test
    fun `Lærling blir mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage(testMessage(lærling = true))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).lærling shouldBe true

        testRapid.sendTestMessage(testMessage(lærling = false))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).lærling shouldBe false

        testRapid.sendTestMessage(testMessage(lærling = null))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).lærling shouldBe false

        testRapid.sendTestMessage(testMessage(lærling = 100))
        shouldThrow<IllegalArgumentException> {
            packetToFakta(behovløser.packet, GrunnbeløpStrategy()).lærling
        }
    }

    @Test
    fun `Beregningsdato blir mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)

        val beregningsdato = LocalDate.of(2022, 1, 1)
        testRapid.sendTestMessage(testMessage(beregningsdato = beregningsdato))
        val fakta = packetToFakta(behovløser.packet, GrunnbeløpStrategy())
        fakta.beregningsdato shouldBe beregningsdato
        fakta.grunnbeløp shouldBe BigDecimal(106399)

        testRapid.sendTestMessage(testMessage(beregningsdato = 100))
        shouldThrow<DateTimeParseException> {
            packetToFakta(behovløser.packet, GrunnbeløpStrategy()).beregningsdato
        }
    }

    @Test
    fun `Regelverksdato mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage(testMessage(regelverksdato = LocalDate.MIN))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).regelverksdato shouldBe LocalDate.MIN

        testRapid.sendTestMessage(testMessage(regelverksdato = 100))
        shouldThrow<DateTimeParseException> {
            packetToFakta(behovløser.packet, GrunnbeløpStrategy()).regelverksdato
        }

        val beregningsdato = LocalDate.MAX
        testRapid.sendTestMessage(testMessage(regelverksdato = null, beregningsdato = beregningsdato))
        packetToFakta(behovløser.packet, GrunnbeløpStrategy()).regelverksdato shouldBe beregningsdato
    }

    @Test
    fun `Brukt inntektsperiode blir mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)
        val førsteMåned = YearMonth.of(2019, 5)
        val sisteMåned = YearMonth.of(2020, 4)

        testRapid.sendTestMessage(
            testMessage(
                bruktInntektsperiode =
                mapOf(
                    "førsteMåned" to førsteMåned,
                    "sisteMåned" to sisteMåned,
                ),
            ),
        )
        packetToFakta(
            behovløser.packet,
            GrunnbeløpStrategy(),
        ).bruktInntektsperiode shouldBe InntektsPeriode(førsteMåned = førsteMåned, sisteMåned = sisteMåned)

        testRapid.sendTestMessage(
            testMessage(
                bruktInntektsperiode = null,
            ),
        )
        packetToFakta(
            behovløser.packet,
            GrunnbeløpStrategy(),
        ).bruktInntektsperiode shouldBe null

        testRapid.sendTestMessage(
            testMessage(
                bruktInntektsperiode =
                mapOf(
                    "MikkeMus" to førsteMåned,
                    "sisteMåned" to sisteMåned,
                ),
            ),
        )
        shouldThrow<IllegalArgumentException> {
            packetToFakta(
                behovløser.packet,
                GrunnbeløpStrategy(),
            )
        }

        testRapid.sendTestMessage(
            testMessage(
                bruktInntektsperiode =
                mapOf(
                    "førsteMåned" to førsteMåned,
                    "sisteMåned" to "DonaldDuck",
                ),
            ),
        )
        shouldThrow<IllegalArgumentException> {
            packetToFakta(
                behovløser.packet,
                GrunnbeløpStrategy(),
            )
        }
    }

    @Test
    fun `Inntekt blir mappet riktig`() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage(
            testMessage(
                inntekt = inntektMap(),
            ),
        )

        packetToFakta(
            behovløser.packet,
            GrunnbeløpStrategy(),
        ).inntekt.let {
            it.inntektsId shouldBe "id3a"
            it.inntektsListe.size shouldBe 2
        }
    }

    @Test
    fun `Should have the grunnbeløp without "gjustering" when beregningsdato is before justering date and featureflag is on `() {
        val unleash = FakeUnleash().also { it.enable(GJUSTERING_TEST) }
        val behovløser = OnPacketTestListener(testRapid)

        val testMessage = testMessage(
            beregningsdato = LocalDate.of(2019, 5, 30),
            fangstOgFiske = true,
        )
        testRapid.sendTestMessage(testMessage)

        val fakta = packetToFakta(behovløser.packet, GrunnbeløpStrategy(unleash))

        fakta.grunnbeløp shouldBe 99858.toBigDecimal()
    }

    @Test
    fun `Should have grunnbeløp with "gjustering" when beregningsdato is after justering date and featureflag is on`() {
        val unleash = FakeUnleash().also { it.enable(GJUSTERING_TEST) }
        val behovløser = OnPacketTestListener(testRapid)

        val testMessage = testMessage(
            fangstOgFiske = true,
            beregningsdato = LocalDate.of(2024, 5, 15),
        )
        testRapid.sendTestMessage(testMessage)

        val fakta = packetToFakta(behovløser.packet, GrunnbeløpStrategy(unleash))

        fakta.grunnbeløp shouldBe 124028.toBigDecimal()
    }

    @Test
    fun `Should have grunnbeløp without "gjustering" when beregningsdato is after justering date and featureflag is off`() {
        val unleash = FakeUnleash().also { it.enable(GJUSTERING_TEST) }
        val behovløser = OnPacketTestListener(testRapid)

        val testMessage = testMessage(
            fangstOgFiske = true,
            beregningsdato = LocalDate.of(2020, 9, 2),
        )
        testRapid.sendTestMessage(testMessage)

        val fakta = packetToFakta(behovløser.packet, GrunnbeløpStrategy(unleash.apply { this.disableAll() }))

        fakta.grunnbeløp shouldBe 99858.toBigDecimal()
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

    private fun inntektMap() =
        mapOf(
            "inntektsId" to "id3a",
            "inntektsListe" to
                listOf(
                    mapOf(
                        "årMåned" to "2020-10",
                        "klassifiserteInntekter" to
                            listOf(
                                mapOf("beløp" to "400000", "inntektKlasse" to "ARBEIDSINNTEKT"),
                                mapOf("beløp" to "400000.5", "inntektKlasse" to "DAGPENGER"),
                            ),
                    ),
                    mapOf(
                        "årMåned" to "2020-11",
                        "klassifiserteInntekter" to
                            listOf(
                                mapOf("beløp" to 400000.0, "inntektKlasse" to "ARBEIDSINNTEKT"),
                                mapOf("beløp" to 100000, "inntektKlasse" to "DAGPENGER"),
                            ),
                    ),
                ),
            "manueltRedigert" to false,
            "sisteAvsluttendeKalenderMåned" to "2023-09",
        )
}
