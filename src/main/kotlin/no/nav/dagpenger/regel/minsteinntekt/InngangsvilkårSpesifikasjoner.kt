package no.nav.dagpenger.regel.minsteinntekt

import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.specifications.Spesifikasjon

// https://www.nav.no/rettskildene/lov/L19970228-19_P4-18#L19970228-19_P4-18
// https://www.nav.no/rettskildene/lov/L19970228-19_P4-4#L19970228-19_P4-4
// https://www.nav.no/rettskildene/lov/L19970228-19_P4-4#L19970228-19_P4-19

internal val verneplikt: Spesifikasjon<Fakta> = Spesifikasjon(
    beskrivelse = "§ 4-19 Dagpenger etter avtjent verneplikt",
    identitet = "§ 4-19",
    implementasjon = { fakta ->
        if (fakta.verneplikt) {
            Evaluering.ja("Oppfylt etter § 4-19 Dagpenger etter avtjent verneplikt")
        } else {
            Evaluering.nei("Ikke oppfylt etter § 4-19 Dagpenger etter avtjent verneplikt")
        }
    }
)

internal val ordinærSiste12Måneder = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-4. Krav til minsteinntekt, minst 1,5 ganger grunnbeløp siste 12 måneder",
    identitet = "§ 4-4 12mnd",
    implementasjon = { fakta ->
        if (fakta.arbeidsinntektSiste12 >= (fakta.grunnbeløp.times(1.5.toBigDecimal()))) {
            Evaluering.ja("Oppfylt etter § 4-4. Krav til minsteinntekt - minst 1,5 ganger grunnbeløp siste 12 måneder")
        } else {
            Evaluering.nei("Ikke Oppfylt etter § 4-4. Krav til minsteinntekt - minst 1,5 ganger grunnbeløp siste 12 måneder")
        }
    }
)

internal val ordinærSiste36Måneder = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-4. Krav til minsteinntekt, minst 3 ganger grunnbeløpet siste 36 måneder",
    identitet = "§ 4-4 36mnd",
    implementasjon = { fakta ->
        if (fakta.arbeidsinntektSiste36 >= (fakta.grunnbeløp.times(3.toBigDecimal()))) {
            Evaluering.ja("Oppfylt etter § 4-4. Minsteinntekt - minst 3 ganger grunnbeløpet siste 36 måneder")
        } else {
            Evaluering.nei("Ikke oppfylt etter § 4-4. Minsteinntekt - minst 3 ganger grunnbeløpet siste 36 måneder")
        }
    }
)

internal val ordinærSiste12MånederMedFangstOgFiske = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-18 - Fangs og Fiske - minst 1,5 ganger grunnbeløp siste 12 måneder",
    identitet = "§ 4-18 12mnd",
    implementasjon = { fakta ->
        if (fakta.fangstOgFisk && fakta.inntektSiste12inkludertFangstOgFiske >= (fakta.grunnbeløp.times(1.5.toBigDecimal()))) {
            Evaluering.ja("Oppfylt etter § 4-18 - Fangs og Fiske - minst 1,5 ganger grunnbeløp siste 12 måneder")
        } else {
            Evaluering.nei("Ikke oppfylt etter § 4-18 - Fangs og Fiske - minst 1,5 ganger grunnbeløp siste 12 måneder")
        }
    }
)

internal val ordinærSiste36MånederMedFangstOgFiske = Spesifikasjon<Fakta>(
    beskrivelse = "§ 4-18 - Fangs og Fiske -, minst 3 ganger grunnbeløpet siste 36 måneder",
    identitet = "§ 4-18 36mnd",
    implementasjon = { fakta ->
        if (fakta.fangstOgFisk && fakta.inntektSiste36inkludertFangstOgFiske >= (fakta.grunnbeløp.times(3.toBigDecimal()))) {
            Evaluering.ja("Oppfylt etter § 4-18 - Fangs og Fiske - minst 3 ganger grunnbeløpet siste 36 måneder")
        } else {
            Evaluering.nei("Ikke oppfylt etter § 4-18 - Fangs og Fiske - minst 3 ganger grunnbeløpet siste 36 måneder")
        }
    }
)

internal val ordinær: Spesifikasjon<Fakta> =
    (ordinærSiste12Måneder eller ordinærSiste36Måneder).eller(ordinærSiste12MånederMedFangstOgFiske eller ordinærSiste36MånederMedFangstOgFiske)
        .med(
            identitet = "§ 4-4 § 4-18",
            beskrivelse = "§ 4-4 eller § 4-18 Krav til minsteinntekt etter ordinær/fangs og fiske inntekt oppfyllt?"
        )

internal val inngangsVilkår: Spesifikasjon<Fakta> = (ordinær eller verneplikt).med(
    identitet = "urn:dp:iv:minsteinntekt:verneplikt:v1",
    beskrivelse = "§ 4-4 og § 4-18 eller § 4-19. inngangsvilkår oppfyllt?"
)