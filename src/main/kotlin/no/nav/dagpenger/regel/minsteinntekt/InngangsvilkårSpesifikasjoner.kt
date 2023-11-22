package no.nav.dagpenger.regel.minsteinntekt

import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.specifications.Spesifikasjon

// https://www.nav.no/rettskildene/lov/L19970228-19_P4-18#L19970228-19_P4-18
// https://www.nav.no/rettskildene/lov/L19970228-19_P4-4#L19970228-19_P4-4
// https://www.nav.no/rettskildene/lov/L19970228-19_P4-4#L19970228-19_P4-19

internal val verneplikt =
    Spesifikasjon<Fakta>(
        beskrivelse = "Krav til minsteinntekt etter § 4-19 - dagpenger etter avtjent verneplikt",
        identifikator = "Krav til minsteinntekt etter § 4-19",
        children = emptyList(),
        implementasjon = {
            when {
                verneplikt -> Evaluering.ja("Verneplikt er avtjent i henhold til kravet")
                else -> Evaluering.nei("Verneplikt er ikke avtjent i henhold til kravet")
            }
        },
    )

internal val kravTilMinsteinntekt: Spesifikasjon<Fakta> =
    (ordinær eller verneplikt)
        .med(
            identifikator = "Krav til minsteinntekt",
            beskrivelse = "Krav til minsteinntekt",
        )

internal val kravTilMinsteinntektKorona: Spesifikasjon<Fakta> =
    (koronaOrdinær eller verneplikt eller lærling)
        .med(
            identifikator = "Krav til minsteinntekt",
            beskrivelse = "Krav til minsteinntekt",
        )

fun Evaluering.finnRegelBrukt() =
    if (this.children.any { it.identifikator == koronaOrdinær.identifikator || it.identifikator == lærling.identifikator }) {
        Beregningsregel.KORONA
    } else {
        Beregningsregel.ORDINAER
    }
