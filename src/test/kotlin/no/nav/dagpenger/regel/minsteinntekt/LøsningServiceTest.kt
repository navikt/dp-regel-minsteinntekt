package no.nav.dagpenger.regel.minsteinntekt

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldNotBe
import java.math.BigDecimal
import java.time.YearMonth
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.LøsningService.Companion.MINSTEINNTEKT
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class LøsningServiceTest {

    companion object {
        private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    private val rapid = TestRapid().apply {
        LøsningService(rapidsConnection = this)
    }

    @BeforeEach
    fun setUp() {
        rapid.reset()
    }

    @Test
    fun ` Skal innhente løsning for minsteinntekt`() {
        rapid.sendTestMessage(
            packetJson
        )

        assertSoftly {

            val inspektør = rapid.inspektør
            inspektør.size shouldBeExactly 1

            inspektør.field(0, "@behov").map(JsonNode::asText) shouldContain MINSTEINNTEKT
            inspektør.field(0, "@løsning") shouldNotBe null
            inspektør.field(0, "@løsning")[MINSTEINNTEKT] shouldNotBe null
            inspektør.field(0, "@løsning")[MINSTEINNTEKT]["minsteinntektNareEvaluering"] shouldNotBe null
            inspektør.field(0, "@løsning")[MINSTEINNTEKT]["minsteinntektInntektsPerioder"] shouldNotBe null
            inspektør.field(0, "@løsning")[MINSTEINNTEKT]["minsteinntektResultat"] shouldNotBe null
        }
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
