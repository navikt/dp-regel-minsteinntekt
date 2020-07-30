package no.nav.dagpenger.regel.minsteinntekt.inngangsvilkårOrdinær

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Fakta
import no.nav.dagpenger.regel.minsteinntekt.generate12MånederFangstOgFiskInntekt
import no.nav.dagpenger.regel.minsteinntekt.generate36MånederFangstOgFiskInntekt
import no.nav.dagpenger.regel.minsteinntekt.generateArbeidsinntekt
import no.nav.dagpenger.regel.minsteinntekt.generateFangstOgFiskInntekt
import no.nav.dagpenger.regel.minsteinntekt.ordinærSiste12Måneder
import no.nav.dagpenger.regel.minsteinntekt.ordinærSiste12MånederMedFangstOgFiske
import no.nav.dagpenger.regel.minsteinntekt.ordinærSiste36MånederMedFangstOgFiske
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

internal class InngangsvilkårFangstOgFiskeSpesifikasjonsTest {

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du ikke har inntekt siste 12 mnd`() {

        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2019, 5, 10)
        )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har tjent litt for lite siste 12 mnd`() {

        val inntekt =
            generateFangstOgFiskInntekt(3, BigDecimal(1))

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2019, 5, 10),
            grunnbeløp = BigDecimal(4)
        )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du ikke hatt nok inntekt siste 12 mnd, på grunn av minus-inntekt`() {

        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2019, 3),
                klassifiserteInntekter = listOf(
                    KlassifisertInntekt(
                        beløp = BigDecimal(1000000),
                        inntektKlasse = InntektKlasse.FANGST_FISKE
                    ),
                    KlassifisertInntekt(
                        beløp = BigDecimal(-950000),
                        inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2019, 5, 10)
        )

        assertEquals(50000.toBigDecimal(), fakta.inntektSiste12inkludertFangstOgFiske)

        val evaluering = ordinærSiste12Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom summen av inntekt blir negativ`() {

        val inntekt = listOf(
            KlassifisertInntektMåned(
                YearMonth.of(2019, 3),
                klassifiserteInntekter = listOf(
                    KlassifisertInntekt(
                        beløp = BigDecimal(1000000),
                        inntektKlasse = InntektKlasse.FANGST_FISKE
                    ),
                    KlassifisertInntekt(
                        beløp = BigDecimal(-1950000),
                        inntektKlasse = InntektKlasse.ARBEIDSINNTEKT
                    )
                )
            )
        )

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2019, 5, 10)
        )

        assertEquals((-950000).toBigDecimal(), fakta.inntektSiste12inkludertFangstOgFiske)

        val evaluering = ordinærSiste12Måneder.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal gi rett til dagpenger i følge § 4-4 dersom du har hatt nok inntekt siste 12 mnd`() {

        val inntekt = generate12MånederFangstOgFiskInntekt()

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2019, 5, 10)
        )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du ikke har inntekt siste 36 mnd`() {

        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList(), sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2019, 5, 10)
        )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har tjent litt for lite siste 36 mnd`() {

        val inntekt =
            generateFangstOgFiskInntekt(24, BigDecimal(1))

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2019, 5, 10),
            grunnbeløp = BigDecimal(23)
        )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal gi rett til dagpenger i følge § 4-4 dersom du har hatt nok inntekt siste 36 mnd`() {

        val inntekt = generate36MånederFangstOgFiskInntekt()

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2019, 5, 10)
        )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal gi rett til dagpenger i følge § 4-4 dersom man har bare arbeidsinntekt siste 12 mnd, selv om fangst og fiske er oppfylt `() {

        val inntekt =
            generateArbeidsinntekt(12, BigDecimal(50000))

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = true,
            beregningsdato = LocalDate.of(2019, 5, 10)
        )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom man har næringsinntekt siste 36 mnd, men er fangst og fisk er ikke oppfylt `() {

        val inntekt =
            generateArbeidsinntekt(36, BigDecimal(50000))

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt, sisteAvsluttendeKalenderMåned = YearMonth.of(2019, 4)),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = false,
            beregningsdato = LocalDate.of(2019, 5, 10)
        )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }
}
