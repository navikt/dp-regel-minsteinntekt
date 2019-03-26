package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth
import kotlin.test.assertEquals

internal class InngangsvilkårFangstOgFiskeSpesifikasjonsTest {

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du ikke har inntekt siste 12 mnd`() {

        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList()),
            senesteInntektsMåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = true
        )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har tjent litt for lite siste 12 mnd`() {

        val inntekt = generateFangstOgFiskInntekt(3, BigDecimal(1))

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt),
            senesteInntektsMåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = true,
            grunnbeløp = BigDecimal(4)
        )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal gi rett til dagpenger i følge § 4-4 dersom du har hatt nok inntekt siste 12 mnd`() {

        val inntekt = generate12MånederFangstOgFiskInntekt()

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt),
            senesteInntektsMåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = true
        )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du ikke har inntekt siste 36 mnd`() {

        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList()),
            senesteInntektsMåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = true
        )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom du har tjent litt for lite siste 36 mnd`() {

        val inntekt = generateFangstOgFiskInntekt(24, BigDecimal(1))

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt),
            senesteInntektsMåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = true,
            grunnbeløp = BigDecimal(23)
        )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }

    @Test
    fun `Skal gi rett til dagpenger i følge § 4-4 dersom du har hatt nok inntekt siste 36 mnd`() {

        val inntekt = generate36MånederFangstOgFiskInntekt()

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt),
            senesteInntektsMåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = true
        )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal gi rett til dagpenger i følge § 4-4 dersom man har bare arbeidsinntekt siste 12 mnd, selv om fangst og fiske er oppfylt `() {

        val inntekt = generateArbeidsinntekt(12, BigDecimal(50000))

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt),
            senesteInntektsMåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = true
        )

        val evaluering = ordinærSiste12MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun `Skal ikke gi rett til dagpenger i følge § 4-4 dersom man har næringsinntekt siste 36 mnd, men er fangst og fisk er ikke oppfylt `() {

        val inntekt = generateArbeidsinntekt(36, BigDecimal(50000))

        val fakta = Fakta(
            inntekt = Inntekt("123", inntekt),
            senesteInntektsMåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = false
        )

        val evaluering = ordinærSiste36MånederMedFangstOgFiske.evaluer(fakta)

        assertEquals(Resultat.NEI, evaluering.resultat)
    }
}