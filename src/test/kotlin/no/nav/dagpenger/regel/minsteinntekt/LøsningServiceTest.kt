package no.nav.dagpenger.regel.minsteinntekt

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeExactly
import java.math.BigDecimal
import java.time.YearMonth
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.LøsningService.Companion.MINSTEINNTEKT
import no.nav.helse.rapids_rivers.InMemoryRapid
import no.nav.helse.rapids_rivers.inMemoryRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class LøsningServiceTest {

    companion object {
        private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    private lateinit var rapid: InMemoryRapid

    @BeforeEach
    fun setUp() {
        rapid = createRapid {
            LøsningService(rapidsConnection = it)
        }
    }

    @Test
    fun ` Skal innhente løsning for minsteinntekt`() {
        rapid.sendToListeners(
            packetJson
        )

        assertSoftly {
            validateMessages(rapid) { messages ->
                messages.size shouldBeExactly 1

                messages.first().also { message ->
                    message["@behov"].map(JsonNode::asText) shouldContain MINSTEINNTEKT
                    message.hasNonNull("@løsning")
                    message["@løsning"].hasNonNull("minsteinntektNareEvaluering")
                    message["@løsning"].hasNonNull("minsteinntektInntektsPerioder")
                    message["@løsning"].hasNonNull("minsteinntektResultat")
                }
            }
        }
    }

    private fun validateMessages(rapid: InMemoryRapid, assertions: (messages: List<JsonNode>) -> Any) {
        rapid.outgoingMessages.map { jacksonObjectMapper().readTree(it.value) }.also { assertions(it) }
    }

    private fun createRapid(service: (InMemoryRapid) -> Any): InMemoryRapid {
        return inMemoryRapid { }.also { service(it) }
    }

    private val inntekt = Inntekt(
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

    private val packetJson =
        """
             {
                "@behov": ["$MINSTEINNTEKT"],
                "@id": "12345", 
                "aktørId": "1234",
                "beregningsdato": "2020-04-21",
                "harAvtjentVerneplikt": true,
                "oppfyllerKravTilFangstOgFisk": false,
                "inntektV1": {
                    "inntektsId": "12345",
                    "manueltRedigert": false,
                    "sisteAvsluttendeKalenderMåned": "2018-02",
                    "inntektsListe": [{
                        "årMåned": "2018-02",
                        "klassifiserteInntekter": [{
                            "beløp": 25000,
                            "inntektKlasse": "ARBEIDSINNTEKT"
                        }]
                    }]
                },
                "bruktInntektsPeriode": {
                    "førsteMåned": "2020-01",
                    "sisteMåned": "2020-04"
                }
             }
            """.trimIndent()
}
