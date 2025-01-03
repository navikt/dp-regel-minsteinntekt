package no.nav.dagpenger.regel.minsteinntekt

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.MINSTEINNTEKT_RESULTAT
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektSubsumsjon.Companion.BEREGNINGSREGEL
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektSubsumsjon.Companion.OPPFYLLER_MINSTEINNTEKT
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class MinsteinntektBehovløserTest {
    private val testRapid = TestRapid()

    init {
        MinsteinntektBehovløser(testRapid)
    }

    @Test
    fun `Skal legge til minsteinntektsubsumsjon`() {
        testRapid.sendTestMessage(inputJson)

        testRapid.inspektør.size shouldBe 1
        val resultatJson = testRapid.inspektør.message(0)
        resultatJson[MINSTEINNTEKT_RESULTAT]["sporingsId"] shouldNotBe null
        resultatJson[MINSTEINNTEKT_RESULTAT]["subsumsjonsId"] shouldNotBe null
        resultatJson.toString().let { resultJson ->
            resultJson shouldEqualSpecifiedJsonIgnoringOrder minsteinntektResultat
            resultJson shouldEqualSpecifiedJsonIgnoringOrder inntektsPerioder
        }
    }

    @Test
    fun `Skal legge til minsteinntektsubsumsjon og makere at inntekt er fra fangst og fiske`() {
        testRapid.sendTestMessage(inputJsonMedInntektFraFangstOgFiske)

        testRapid.inspektør.size shouldBe 1
        val resultatJson = testRapid.inspektør.message(0)
        resultatJson[MINSTEINNTEKT_RESULTAT]["sporingsId"] shouldNotBe null
        resultatJson[MINSTEINNTEKT_RESULTAT]["subsumsjonsId"] shouldNotBe null
        resultatJson.toString().let { resultJson ->
            resultJson shouldEqualSpecifiedJsonIgnoringOrder minsteinntektResultat
            resultJson shouldEqualSpecifiedJsonIgnoringOrder inntektsperioderMedFangstOgFiske
        }
    }

    @Test
    fun `Skal legge til system_problem`() {
        shouldThrowAny {
            testRapid.sendTestMessage(feilJson)
        }
        testRapid.inspektør.size shouldBe 1
        testRapid.inspektør.message(0).toString().let { resultJson ->
            resultJson shouldEqualSpecifiedJsonIgnoringOrder """
              {
                  "system_problem": {
                    "type": "urn:dp:error:regel",
                    "title": "Ukjent feil ved bruk av minsteinntektregel",
                    "status": 500,
                    "instance": "urn:dp:regel:minsteinntekt"
                  }
              }
            """
        }
    }

    @ParameterizedTest
    @CsvSource(
        "2020-03-19, ORDINAER",
        "2020-03-20, KORONA",
        "2020-10-31, KORONA",
        "2020-11-01, ORDINAER",
        "2021-02-18, ORDINAER",
        "2021-02-19, KORONA",
        "2021-09-30, KORONA",
        "2021-10-01, ORDINAER",
        "2021-12-14, ORDINAER",
        "2021-12-15, KORONA",
        "2022-03-31, KORONA",
        "2022-04-01, ORDINAER",
    )
    fun `Skal benytte korrekt beregningsregel avhengig av beregningsdato`(
        beregningsdato: String,
        regel: String,
    ) {
        testRapid.sendTestMessage(testMessage(beregningsdato, beløp = "25000", årMånedForInntekt = "2019-02"))
        val resultatPacket = testRapid.inspektør.message(0)
        resultatPacket[MINSTEINNTEKT_RESULTAT][BEREGNINGSREGEL].asText() shouldBe Beregningsregel.valueOf(regel).name
    }

    @ParameterizedTest
    @CsvSource(
        "false, 2020-03-19",
        "true, 2020-03-20",
        "true, 2020-11-20",
        "true, 2021-09-30",
        "false, 2021-10-01",
        "false, 2021-12-14",
        "true, 2021-12-15",
        "true, 2022-03-31",
        "false, 2022-04-01",
    )
    fun `Skal evaluere minsteinntekt for lærlingeperiode`(
        oppfyllerMinstearbeidsinntekt: Boolean,
        beregningsdato: String,
    ) {
        val testMessage = testMessage(beregningsdato = beregningsdato, erLærling = true, beløp = "98866", årMånedForInntekt = "2020-02")
        testRapid.sendTestMessage(testMessage)
        val resultatPacket = testRapid.inspektør.message(0)
        resultatPacket[MINSTEINNTEKT_RESULTAT][OPPFYLLER_MINSTEINNTEKT].asBoolean() shouldBe oppfyllerMinstearbeidsinntekt
    }
}

@Language("JSON")
fun testMessage(
    beregningsdato: String,
    erLærling: Boolean = false,
    beløp: String,
    årMånedForInntekt: String,
) = """
    {
        "behovId": "behovId",
        "harAvtjentVerneplikt": false,
        "lærling": $erLærling,
        "oppfyllerKravTilFangstOgFisk": false,
        "beregningsDato": "$beregningsdato",
        "inntektV1": {
          "inntektsId": "12345",
          "inntektsListe": [
            {
              "årMåned": "$årMånedForInntekt",
              "klassifiserteInntekter": [
                {
                  "beløp": "$beløp",
                  "inntektKlasse": "ARBEIDSINNTEKT"
                }
              ]
            }
          ],
          "manueltRedigert": false,
          "sisteAvsluttendeKalenderMåned": "$årMånedForInntekt"
        }
    }
    """.trimIndent()

@Language("JSON")
private val minsteinntektResultat = """{
  "minsteinntektResultat": {
    "regelIdentifikator": "Minsteinntekt.v1",
    "oppfyllerMinsteinntekt": false,
    "beregningsregel": "ORDINAER"
  }
}"""

@Language("JSON")
private val inntektsPerioder = """{
  "minsteinntektInntektsPerioder": [
    {
      "inntektsPeriode": {
        "førsteMåned": "2018-03",
        "sisteMåned": "2019-02"
      },
      "inntekt": 25000,
      "periode": 1,
      "inneholderFangstOgFisk": false,
      "andel": 25000
    },
    {
      "inntektsPeriode": {
        "førsteMåned": "2017-03",
        "sisteMåned": "2018-02"
      },
      "inntekt": 0,
      "periode": 2,
      "inneholderFangstOgFisk": false,
      "andel": 0
    },
    {
      "inntektsPeriode": {
        "førsteMåned": "2016-03",
        "sisteMåned": "2017-02"
      },
      "inntekt": 0,
      "periode": 3,
      "inneholderFangstOgFisk": false,
      "andel": 0
    }
  ]
}"""

@Language("JSON")
private val inntektsperioderMedFangstOgFiske = """{
  "minsteinntektInntektsPerioder": [
    {
      "inntektsPeriode": {
        "førsteMåned": "2018-03",
        "sisteMåned": "2019-02"
      },
      "inntekt": 25000,
      "periode": 1,
      "inneholderFangstOgFisk": true,
      "andel": 25000
    },
    {
      "inntektsPeriode": {
        "førsteMåned": "2017-03",
        "sisteMåned": "2018-02"
      },
      "inntekt": 0,
      "periode": 2,
      "inneholderFangstOgFisk": false,
      "andel": 0
    },
    {
      "inntektsPeriode": {
        "førsteMåned": "2016-03",
        "sisteMåned": "2017-02"
      },
      "inntekt": 0,
      "periode": 3,
      "inneholderFangstOgFisk": false,
      "andel": 0
    }
  ]
}"""

@Language("JSON")
private val inputJson =
    """
        {
          "behovId": "behovId",
          "beregningsDato": "2019-02-27",
          "harAvtjentVerneplikt": false,
          "bruktInntektsPeriode": {
            "førsteMåned": "2016-02",
            "sisteMåned": "2016-11"
          },
          "inntektV1": {
            "inntektsId": "12345",
            "inntektsListe": [
              {
                "årMåned": "2019-02",
                "klassifiserteInntekter": [
                  {
                    "beløp": "25000",
                    "inntektKlasse": "ARBEIDSINNTEKT"
                  }
                ]
              }
            ],
            "manueltRedigert": false,
            "sisteAvsluttendeKalenderMåned": "2019-02"
          }
        }
        """

@Language("JSON")
private val inputJsonMedInntektFraFangstOgFiske =
    """
        {
          "behovId": "behovId",
          "beregningsDato": "2019-02-27",
          "harAvtjentVerneplikt": false,
          "bruktInntektsPeriode": {
            "førsteMåned": "2016-02",
            "sisteMåned": "2016-11"
          },
          "inntektV1": {
            "inntektsId": "12345",
            "inntektsListe": [
              {
                "årMåned": "2019-02",
                "klassifiserteInntekter": [
                  {
                    "beløp": "25000",
                    "inntektKlasse": "ARBEIDSINNTEKT"
                  }, 
                  {
                        "beløp": "1000",
                        "inntektKlasse": "FANGST_FISKE"
                    }
                ]
              }
            ],
            "manueltRedigert": false,
            "sisteAvsluttendeKalenderMåned": "2019-02"
          }
        }
        """

@Language("JSON")
val feilJson =
    """
    {
      "behovId": "behovId",
      "beregningsDato": "2020-05-20",
      "inntektV1": "ERROR"
    } 
    """.trimIndent()
