package no.nav.dagpenger.regel.minsteinntekt

import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.specifications.Spesifikasjon

// https://www.nav.no/rettskildene/lov/L19970228-19_P4-18#L19970228-19_P4-18
// https://www.nav.no/rettskildene/lov/L19970228-19_P4-4#L19970228-19_P4-4
// https://www.nav.no/rettskildene/lov/L19970228-19_P4-4#L19970228-19_P4-19

internal val verneplikt = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-19 Dagpenger etter avtjent verneplikt",
    identifikator = "§ 4-19",
    children = emptyList(),
    implementasjon = {
        when {
            verneplikt -> Evaluering.ja("Oppfylt etter § 4-19 Dagpenger etter avtjent verneplikt")
            else -> Evaluering.nei("Ikke oppfylt etter § 4-19 Dagpenger etter avtjent verneplikt")
        }
    }
)

internal val ordinærSiste12Måneder = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-4. Krav til minsteinntekt, minst 1,5 ganger grunnbeløp siste 12 måneder",
    identifikator = "§ 4-4 12mnd",
    implementasjon = {
        when {
            arbeidsinntektSiste12 >= (grunnbeløp.times(1.5.toBigDecimal())) -> Evaluering.ja(
                "Oppfylt etter § 4-4. Krav til minsteinntekt - minst 1,5 ganger grunnbeløp siste 12 måneder"
            )
            else -> Evaluering.nei("Ikke Oppfylt etter § 4-4. Krav til minsteinntekt - minst 1,5 ganger grunnbeløp siste 12 måneder")
        }
    }
)

internal val ordinærSiste36Måneder = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-4. Krav til minsteinntekt, minst 3 ganger grunnbeløpet siste 36 måneder",
    identifikator = "§ 4-4 36mnd",
    implementasjon = {
        when {
            arbeidsinntektSiste36 >= (grunnbeløp.times(3.toBigDecimal())) -> Evaluering.ja(
                "Oppfylt etter § 4-4. Minsteinntekt - minst 3 ganger grunnbeløpet siste 36 måneder"
            )
            else -> Evaluering.nei("Ikke oppfylt etter § 4-4. Minsteinntekt - minst 3 ganger grunnbeløpet siste 36 måneder")
        }
    }
)

internal val ordinærSiste12MånederMedFangstOgFiske = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-18 - Fangs og Fiske - minst 1,5 ganger grunnbeløp siste 12 måneder",
    identifikator = "§ 4-18 12mnd",
    implementasjon = {
        when {
            fangstOgFisk && inntektSiste12inkludertFangstOgFiske >= (grunnbeløp.times(1.5.toBigDecimal())) -> Evaluering.ja(
                "Oppfylt etter § 4-18 - Fangs og Fiske - minst 1,5 ganger grunnbeløp siste 12 måneder"
            )
            else -> Evaluering.nei("Ikke oppfylt etter § 4-18 - Fangs og Fiske - minst 1,5 ganger grunnbeløp siste 12 måneder")
        }
    }
)

internal val ordinærSiste36MånederMedFangstOgFiske = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-18 - Fangs og Fiske -, minst 3 ganger grunnbeløpet siste 36 måneder",
    identifikator = "§ 4-18 36mnd",
    implementasjon = {
        when {
            fangstOgFisk && inntektSiste36inkludertFangstOgFiske >= (grunnbeløp.times(3.toBigDecimal())) -> Evaluering.ja(
                "Oppfylt etter § 4-18 - Fangs og Fiske - minst 3 ganger grunnbeløpet siste 36 måneder"
            )
            else -> Evaluering.nei("Ikke oppfylt etter § 4-18 - Fangs og Fiske - minst 3 ganger grunnbeløpet siste 36 måneder")
        }
    }
)

internal val ordinær: Spesifikasjon<Fakta> =
    (ordinærSiste12Måneder eller ordinærSiste36Måneder).eller(ordinærSiste12MånederMedFangstOgFiske eller ordinærSiste36MånederMedFangstOgFiske)

internal val inngangsVilkår: Spesifikasjon<Fakta> = (ordinær eller verneplikt)