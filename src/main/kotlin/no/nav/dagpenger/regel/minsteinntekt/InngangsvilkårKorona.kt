package no.nav.dagpenger.regel.minsteinntekt

import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.specifications.Spesifikasjon

internal val koronaOrdinærSiste12Måneder = Spesifikasjon<Fakta>(
    beskrivelse = "Krav til minsteinntekt etter midlertidig korona-endret § 4-4 første ledd bokstav a - minst 0,75 ganger grunnbeløp siste 12 måneder",
    identifikator = "Krav til minsteinntekt etter midlertidig korona-endret § 4-4 første ledd bokstav a",
    implementasjon = {
        when {
            arbeidsinntektSiste12 >= (grunnbeløp.times(0.75.toBigDecimal())) -> Evaluering.ja(
                "Inntekt siste 12 måneder er lik eller større enn 0,75 ganger grunnbeløp"
            )
            else -> Evaluering.nei("Inntekt siste 12 måneder er mindre enn 0,75 ganger grunnbeløp")
        }
    }
)

internal val koronaOrdinærSiste36Måneder = Spesifikasjon<Fakta>(
    beskrivelse = "Krav til minsteinntekt etter midlertidig korona-endret § 4-4 første ledd bokstav b - minst 2,25 ganger grunnbeløpet siste 36 måneder",
    identifikator = "Krav til minsteinntekt etter midlertidig korona-endret § 4-4 første ledd bokstav b",
    implementasjon = {
        when {
            arbeidsinntektSiste36 >= (grunnbeløp.times(2.25.toBigDecimal())) -> Evaluering.ja(
                "Inntekt siste 36 måneder er lik eller større enn 2,25 ganger grunnbeløp"
            )
            else -> Evaluering.nei("Inntekt siste 36 måneder er mindre enn 2,25 ganger grunnbeløp")
        }
    }
)

internal val koronaOrdinær: Spesifikasjon<Fakta> =
    (koronaOrdinærSiste12Måneder eller koronaOrdinærSiste36Måneder)
        .med(
            identifikator = "Krav til minsteinntekt etter § 4-4",
            beskrivelse = "Krav til minsteinntekt etter ordinære regler"
        )