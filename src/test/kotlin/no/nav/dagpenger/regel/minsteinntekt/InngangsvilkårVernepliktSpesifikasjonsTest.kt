package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.nare.core.evaluations.Resultat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.YearMonth

internal class InngangsvilkårVernepliktSpesifikasjonsTest {

    @Test
    fun ` § 4-19 - Dagpenger etter avtjent verneplikt skal gi rett til Dagpenger  `() {

        // gitt fakta
        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList()),
            fraMåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = true,
            fangstOgFisk = false
        )

        // når
        val evaluering = minsteinntektEtterAvtjentVerneplikt.evaluer(fakta)

        // så
        assertEquals(Resultat.JA, evaluering.resultat)
    }

    @Test
    fun ` § 4-19 - Dagpenger etter ikke å ha avtjent verneplikt skal gi ikke rett til Dagpenger  `() {

        // gitt fakta
        val fakta = Fakta(
            inntekt = Inntekt("123", emptyList()),
            fraMåned = YearMonth.of(2019, 4),
            bruktInntektsPeriode = null,
            verneplikt = false,
            fangstOgFisk = false
        )

        // når
        val evaluering = minsteinntektEtterAvtjentVerneplikt.evaluer(fakta)

        // så
        assertEquals(Resultat.NEI, evaluering.resultat)
    }
}