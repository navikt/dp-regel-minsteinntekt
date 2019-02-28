package no.nav.dagpenger.regel.minsteinntekt

import org.junit.jupiter.api.Test

class SumInntekterTest {

    val jsonBehovMedInntekt = """
        {
            "inntekt": {
                "inntektsId": "12345",
                "inntektsListe": [
                    {
                        "årMåned": "2017-09",
                        "klassifiserteInntekter": [
                            {
                                "beløp": "362000",
                                "inntektKlasse": "ARBEIDSINNTEKT"
                            }
                        ]
                    },
                    {
                        "årMåned": "2017-08",
                        "klassifiserteInntekter": [
                            {
                                "beløp": "18900",
                                "inntektKlasse": "ARBEIDSINNTEKT"
                            }
                        ]
                    },
                    {
                        "årMåned": "2018-04",
                        "klassifiserteInntekter": [
                            {
                                "beløp": "89700",
                                "inntektKlasse": "ARBEIDSINNTEKT"
                            }
                        ]
                    },
                    {
                        "årMåned": "2018-03",
                        "klassifiserteInntekter": [
                            {
                                "beløp": "25000",
                                "inntektKlasse": "ARBEIDSINNTEKT"
                            }
                        ]
                    }
                ]
            }
            }
            """.trimIndent()

    @Test
    fun ` should add Arbeidsinntekt in sumSiste12 `() {
    }

    @Test
    fun ` should add Arbeidsinntekt in sumSiste36 `() {
    }

    @Test
    fun ` should not add næringsinntekt in sumSiste12 when there is no fangst og fisk `() {
    }

    @Test
    fun ` should not add næringsinntekt in sumSiste36 when there is no fangst og fisk `() {
    }
}