package no.nav.dagpenger.regel.minsteinntekt

import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.MINSTEINNTEKT_RESULTAT
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

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
}

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
      "inntekt": "25000",
      "periode": 1,
      "inneholderFangstOgFisk": false,
      "andel": "25000"
    },
    {
      "inntektsPeriode": {
        "førsteMåned": "2017-03",
        "sisteMåned": "2018-02"
      },
      "inntekt": "0",
      "periode": 2,
      "inneholderFangstOgFisk": false,
      "andel": "0"
    },
    {
      "inntektsPeriode": {
        "førsteMåned": "2016-03",
        "sisteMåned": "2017-02"
      },
      "inntekt": "0",
      "periode": 3,
      "inneholderFangstOgFisk": false,
      "andel": "0"
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
      "inntekt": "25000",
      "periode": 1,
      "inneholderFangstOgFisk": true,
      "andel": "25000"
    },
    {
      "inntektsPeriode": {
        "førsteMåned": "2017-03",
        "sisteMåned": "2018-02"
      },
      "inntekt": "0",
      "periode": 2,
      "inneholderFangstOgFisk": false,
      "andel": "0"
    },
    {
      "inntektsPeriode": {
        "førsteMåned": "2016-03",
        "sisteMåned": "2017-02"
      },
      "inntekt": "0",
      "periode": 3,
      "inneholderFangstOgFisk": false,
      "andel": "0"
    }
  ]
}"""

@Language("JSON")
private val inputJson =
    """
        {
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
              "beregningsDato": "2020-05-20",
              "inntektV1": "ERROR"
            } 
    """.trimIndent()
