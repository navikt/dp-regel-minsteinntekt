package no.nav.dagpenger.regel.minsteinntekt.inngangsvilkårOrdinær

import no.nav.dagpenger.inntekt.v1.Inntekt
import no.nav.dagpenger.regel.minsteinntekt.Fakta
import no.nav.dagpenger.regel.minsteinntekt.grunnbeløpStrategy
import no.nav.dagpenger.regel.minsteinntekt.kravTilMinsteinntekt
import no.nav.dagpenger.regel.minsteinntekt.ordinær
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class InngangsvilkårOrdinærIdentifikatorTest {
    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `Minsteinntekt inneholder alle krav etter § 4-4`() {
        assertEquals(
            "Krav til minsteinntekt etter § 4-4 første ledd bokstav a, Krav til minsteinntekt etter § 4-4 første ledd bokstav b, Krav til minsteinntekt etter § 4-18 + § 4-4 første ledd bokstav a, Krav til minsteinntekt etter § 4-18 + § 4-4 første ledd bokstav b",
            ordinær.children.joinToString { it.identifikator },
        )
    }

    @Test
    fun `Minsteinntekt gir JA til verneplikt`() {
        val beregningsdato = LocalDate.now()
        var evaluering =
            kravTilMinsteinntekt.evaluer(
                Fakta(
                    inntekt =
                        Inntekt(
                            inntektsId = "test-inntekt",
                            inntektsListe = listOf(),
                            manueltRedigert = false,
                            sisteAvsluttendeKalenderMåned = YearMonth.of(2001, 11),
                        ),
                    verneplikt = true,
                    fangstOgFiske = false,
                    beregningsdato = beregningsdato,
                    regelverksdato = LocalDate.now(),
                    grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
                ),
            )

        assertSame(Resultat.JA, evaluering.resultat)
        assertEquals(
            "Krav til minsteinntekt etter § 4-4, Krav til minsteinntekt etter § 4-19",
            evaluering.children.joinToString { it.identifikator },
        )
    }
}
