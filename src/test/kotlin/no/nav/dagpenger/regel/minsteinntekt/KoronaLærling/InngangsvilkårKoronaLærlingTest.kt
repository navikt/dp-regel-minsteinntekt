package no.nav.dagpenger.regel.minsteinntekt.KoronaLærling

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Beregningsregel
import no.nav.dagpenger.regel.minsteinntekt.Fakta
import no.nav.dagpenger.regel.minsteinntekt.finnRegelBrukt
import no.nav.dagpenger.regel.minsteinntekt.grunnbeløpStrategy
import no.nav.dagpenger.regel.minsteinntekt.kravTilMinsteinntektKorona
import no.nav.dagpenger.regel.minsteinntekt.lærling
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

internal class InngangsvilkårKoronaLærlingTest {
    private val inntekt = emptyList<KlassifisertInntektMåned>()

    val beregningsdato = LocalDate.of(2020, 12, 10)
    private val fakta = Fakta(
        inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2020, 2)),
        bruktInntektsPeriode = null,
        verneplikt = false,
        fangstOgFisk = false,
        lærling = true,
        beregningsdato = beregningsdato,
        regelverksdato = LocalDate.of(2020, 12, 10),
        grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato)
    )

    @Test
    fun ` Forskrift 2-6 Midlertidig inntekssikringsordning for lærlinger – unntak fra folketrygdloven paragraf 4-4 `() {
        val evaluering = lærling.evaluer(fakta)
        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun ` Forskrift 2-6 Midlertidig inntekssikringsordning gis bare for lærlinger`() {
        val evaluering = lærling.evaluer(fakta.copy(lærling = false))
        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun ` Forskrift 2-6 Midlertidig inntekssikringsordning for lærlinger regel identifikator`() {
        assertEquals("§ 2-6.Midlertidig inntekssikringsordning for lærlinger", lærling.identifikator)
    }

    @Test
    fun ` Forskrift 2-6 Midlertidig inntekssikringsordning for lærlinger er en beregningsregel KORONA `() {
        val evaluering = kravTilMinsteinntektKorona.evaluer(fakta)
        assertEquals(Beregningsregel.KORONA, evaluering.finnRegelBrukt())
    }
}
