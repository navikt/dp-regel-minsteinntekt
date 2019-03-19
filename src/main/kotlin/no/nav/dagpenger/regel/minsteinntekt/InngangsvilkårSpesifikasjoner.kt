package no.nav.dagpenger.regel.minsteinntekt

import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.specifications.Spesifikasjon
import java.math.BigDecimal

val minsteinntektEtterAvtjentVerneplikt: Spesifikasjon<Fakta> = Spesifikasjon(
    beskrivelse = "§ 4-19 Dagpenger etter avtjent verneplikt",
    identitet = "MINSTEINNTEKT_V1_5",
    implementasjon = { fakta ->
        if (fakta.verneplikt) {
            Evaluering.ja("Rett § 4-19 Dagpenger etter avtjent verneplikt ")
        } else {
            Evaluering.nei("Ikke rett § 4-19 Dagpenger etter avtjent verneplikt ")
        }
    }
)

private val ordinærSiste12Måneder = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-4. Krav til minsteinntekt, siste 12 måneder",
    identitet = "ORDINÆR_12",
    implementasjon = { fakta ->
        if (fakta.inntektSiste12 > (fakta.grunnbeløp.times(BigDecimal(1.5)))) {
            Evaluering.ja("Rett til Dagpenger etter § 4-4. Krav til minsteinntekt, siste 12 måneder")
        } else {
            Evaluering.nei("Ikke rett til Dagpenger etter § 4-4. Krav til minsteinntekt, siste 12 måneder")
        }
    }
)

private val ordinærSiste36Måneder = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-4. Krav til minsteinntekt, siste 36 måneder",
    identitet = "ORDINÆR_36",
    implementasjon = { fakta ->
        if (fakta.inntektSiste36 > (fakta.grunnbeløp.times(BigDecimal(3)))) {
            Evaluering.ja("Rett til Dagpenger etter § 4-4. Krav til minsteinntekt, siste 36 måneder")
        } else {
            Evaluering.nei("Ikke rett til Dagpenger etter § 4-4. Krav til minsteinntekt, siste 36 måneder")
        }
    }
)

private val ordinærSiste12MånederEtterNæringsInntekter = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-4. Krav til minsteinntekt, siste 12 måneder, etter næringsinntekt",
    identitet = "ORDINÆR_12_NÆRING",
    implementasjon = { fakta ->
        if (fakta.arbeidsInntektOgNæringsInntektSiste12 > (fakta.grunnbeløp.times(BigDecimal(1.5)))) {
            Evaluering.ja("Rett til Dagpenger etter § 4-4. Krav til minsteinntekt, siste 12 måneder, etter næringsinntekt")
        } else {
            Evaluering.nei("Ikke rett til Dagpenger etter § 4-4. Krav til minsteinntekt, siste 12 måneder, etter næringsinntekt")
        }
    }
)

private val ordinærSiste36MånederEtterNæringsInntekter = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-4. Krav til minsteinntekt, siste 12 måneder, etter næringsinntekt",
    identitet = "ORDINÆR_36_NÆRING",
    implementasjon = { fakta ->
        if (fakta.arbeidsInntektOgNæringsInntektSiste36 > (fakta.grunnbeløp.times(BigDecimal(3)))) {
            Evaluering.ja("Rett til Dagpenger etter § 4-4. Krav til minsteinntekt, siste 12 måneder, etter næringsinntekt")
        } else {
            Evaluering.nei("Ikke rett til Dagpenger etter § 4-4. Krav til minsteinntekt, siste 12 måneder, etter næringsinntekt")
        }
    }
)

val ordninær: Spesifikasjon<Fakta> =
    (ordinærSiste12Måneder eller ordinærSiste36Måneder).eller(ordinærSiste12MånederEtterNæringsInntekter eller ordinærSiste36MånederEtterNæringsInntekter)
        .med(
            identitet = "ORDINÆR",
            beskrivelse = "§ 4-4. Krav til minsteinntekt etter ordinær inntekt oppfyllt?"
        )

val verneplikt: Spesifikasjon<Fakta> = minsteinntektEtterAvtjentVerneplikt.med(
    identitet = "VERNEPLIKT",
    beskrivelse = "§ 4-19. Dagpenger etter avtjent verneplikt"
)

val inngangsVilkår: Spesifikasjon<Fakta> = (ordninær eller verneplikt).med(
    identitet = "INNGANGSVILKÅR",
    beskrivelse = "§ 4-4. eller § 4-19. inngangsvilkår oppfyllt?"
)