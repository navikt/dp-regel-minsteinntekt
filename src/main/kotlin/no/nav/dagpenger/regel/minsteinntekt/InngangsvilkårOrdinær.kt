package no.nav.dagpenger.regel.minsteinntekt

import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.specifications.Spesifikasjon

internal val ordinærSiste12Måneder =
    Spesifikasjon<Fakta>(
        beskrivelse = "Krav til minsteinntekt etter § 4-4 første ledd bokstav a - minst 1,5 ganger grunnbeløp siste 12 måneder",
        identifikator = "Krav til minsteinntekt etter § 4-4 første ledd bokstav a",
        implementasjon = {
            when {
                arbeidsinntektSiste12 >= (grunnbeløp.times(1.5.toBigDecimal())) ->
                    Evaluering.ja(
                        "Inntekt siste 12 måneder er lik eller større enn 1,5 ganger grunnbeløp",
                    )
                else -> Evaluering.nei("Inntekt siste 12 måneder er mindre enn 1,5 ganger grunnbeløp")
            }
        },
    )

internal val ordinærSiste36Måneder =
    Spesifikasjon<Fakta>(
        beskrivelse = "Krav til minsteinntekt etter § 4-4 første ledd bokstav b - minst 3 ganger grunnbeløpet siste 36 måneder",
        identifikator = "Krav til minsteinntekt etter § 4-4 første ledd bokstav b",
        implementasjon = {
            when {
                arbeidsinntektSiste36 >= (grunnbeløp.times(3.toBigDecimal())) ->
                    Evaluering.ja(
                        "Inntekt siste 36 måneder er lik eller større enn 3 ganger grunnbeløp",
                    )
                else -> Evaluering.nei("Inntekt siste 36 måneder er mindre enn 3 ganger grunnbeløp")
            }
        },
    )

internal val ordinærSiste12MånederMedFangstOgFiske =
    Spesifikasjon<Fakta>(
        beskrivelse = "Krav til minsteinntekt etter § 4-18 + § 4-4 første ledd bokstav a - minst 1,5 ganger grunnbeløp siste 12 måneder",
        identifikator = "Krav til minsteinntekt etter § 4-18 + § 4-4 første ledd bokstav a",
        implementasjon = {
            when {
                erGyldigFangstOgFisk() && inntektSiste12inkludertFangstOgFiske >= (grunnbeløp.times(1.5.toBigDecimal())) ->
                    Evaluering.ja(
                        "Inntekt inkludert inntekt fra fangst og fisk siste 12 måneder er lik eller større enn 1,5 ganger grunnbeløp",
                    )
                else -> Evaluering.nei(fangstOgFiskAvslagBegrunnelse(antallOpptjeningsMåneder = "12", antallGangerGrunnbeløp = "1,5"))
            }
        },
    )

internal val ordinærSiste36MånederMedFangstOgFiske =
    Spesifikasjon<Fakta>(
        beskrivelse = "Krav til minsteinntekt etter § 4-18 + § 4-4 første ledd bokstav b - minst 3 ganger grunnbeløp siste 36 måneder",
        identifikator = "Krav til minsteinntekt etter § 4-18 + § 4-4 første ledd bokstav b",
        implementasjon = {
            when {
                erGyldigFangstOgFisk() && inntektSiste36inkludertFangstOgFiske >= (grunnbeløp.times(3.toBigDecimal())) ->
                    Evaluering.ja(
                        "Inntekt inkludert inntekt fra fangst og fisk siste 36 måneder er lik eller større enn 3 ganger grunnbeløp",
                    )
                else -> Evaluering.nei(fangstOgFiskAvslagBegrunnelse(antallOpptjeningsMåneder = "36", antallGangerGrunnbeløp = "3"))
            }
        },
    )

@Suppress("ktlint:standard:max-line-length")
private fun Fakta.fangstOgFiskAvslagBegrunnelse(
    antallOpptjeningsMåneder: String,
    antallGangerGrunnbeløp: String,
) = if (erGyldigFangstOgFisk()) {
    "Inntekt inkludert inntekt fra fangst og fisk siste $antallOpptjeningsMåneder måneder er mindre enn $antallGangerGrunnbeløp ganger grunnbeløp"
} else {
    "Ikke gyldig fangst og fisk. Dette kan skyldes at regelverket avviklet inntekt fra fangst og fisk 01.01.2022"
}

internal val fangstOgFisk = ordinærSiste12MånederMedFangstOgFiske eller ordinærSiste36MånederMedFangstOgFiske

internal val ordinær: Spesifikasjon<Fakta> =
    (ordinærSiste12Måneder eller ordinærSiste36Måneder)
        .eller(fangstOgFisk)
        .med(
            identifikator = "Krav til minsteinntekt etter § 4-4",
            beskrivelse = "Krav til minsteinntekt etter ordinære regler",
        )
