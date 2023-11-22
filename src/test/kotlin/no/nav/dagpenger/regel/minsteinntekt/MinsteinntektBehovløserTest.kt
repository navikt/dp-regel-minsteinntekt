package no.nav.dagpenger.regel.minsteinntekt

import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
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
        testRapid.sendTestMessage(inputJson())

        testRapid.inspektør.size shouldBe 1
        val resultatJson = testRapid.inspektør.message(0)
        resultatJson[MINSTEINNTEKT_RESULTAT]["sporingsId"] shouldNotBe null
        resultatJson[MINSTEINNTEKT_RESULTAT]["subsumsjonsId"] shouldNotBe null
        resultatJson.toString().let { resultJson ->
            assertMinsteinntektResultatFra(resultJson)
            assertInntektsperioderFra(resultJson)
        }
    }

    private fun assertMinsteinntektResultatFra(resultJson: String) {
        //language=JSON
        resultJson shouldEqualSpecifiedJsonIgnoringOrder """{
  "minsteinntektResultat": {
    "regelIdentifikator": "Minsteinntekt.v1",
    "oppfyllerMinsteinntekt": false,
    "beregningsregel": "ORDINAER"
  }
}"""
    }

    @Language("JSON")
    private fun assertInntektsperioderFra(resultJson: String) {
        resultJson shouldEqualSpecifiedJsonIgnoringOrder """{
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
    }
}

@Language("JSON")
fun inputJson() = """
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
