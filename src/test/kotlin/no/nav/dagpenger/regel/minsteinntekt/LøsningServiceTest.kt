package no.nav.dagpenger.regel.minsteinntekt

import com.fasterxml.jackson.databind.JsonNode
import de.huxhorn.sulky.ulid.ULID
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.time.YearMonth
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.inntekt.rpc.InntektHenter
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class LøsningServiceTest {

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

    private val inntektHenter = mockk<InntektHenter>().also {
        every { runBlocking { it.hentKlassifisertInntekt(any()) } } returns inntekt
    }

    private val rapid = TestRapid().apply {
        LøsningService(rapidsConnection = this, inntektHenter = inntektHenter)
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

            inspektør.field(0, "@behov").map(JsonNode::asText) shouldContain "Minsteinntekt"
            inspektør.field(0, "@løsning") shouldNotBe null
            inspektør.field(0, "@løsning")["Minsteinntekt"] shouldNotBe null
            inspektør.field(0, "@løsning")["Minsteinntekt"]["inntektsperioder"] shouldNotBe null
            inspektør.field(0, "@løsning")["Minsteinntekt"]["resultat"] shouldNotBe null
        }
    }

    @Test
    fun ` Skal håndtere feil i inntekthenting `() {
        rapid.sendTestMessage(
            medFeilInntektId
        )

        assertSoftly {
            val inspektør = rapid.inspektør
            inspektør.size shouldBeExactly 0
        }
    }

    @Language("JSON")
    private val packetJson =
        """
             {
                "@behov": ["Minsteinntekt"],
                "@id": "12345", 
                "aktørId": "1234",
                "beregningsdato": "2020-04-21",
                "vedtakId" : "12122",
                "harAvtjentVerneplikt": true,
                "oppfyllerKravTilFangstOgFisk": false,
                "inntektId": "${ULID().nextULID()}",
                "bruktInntektsPeriode": {
                    "førsteMåned": "2020-01",
                    "sisteMåned": "2020-04"
                }
             }
            """.trimIndent()

    @Language("JSON")
    private val medFeilInntektId =
        """
             {
                "@behov": ["Minsteinntekt"],
                "@id": "12345", 
                "aktørId": "1234",
                "vedtakId" : "12122"
                "beregningsdato": "2020-04-21",
                "harAvtjentVerneplikt": true,
                "oppfyllerKravTilFangstOgFisk": false,
                "inntektId": "blabla",
                "bruktInntektsPeriode": {
                    "førsteMåned": "2020-01",
                    "sisteMåned": "2020-04"
                }
             }
            """.trimIndent()
}
