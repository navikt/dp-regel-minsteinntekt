package no.nav.dagpenger.regel.minsteinntekt

import no.nav.nare.core.evaluations.Evaluering
import no.nav.nare.core.specifications.Spesifikasjon
@Suppress("ktlint:standard:max-line-length")
internal val koronaOrdinærSiste12Måneder = Spesifikasjon<Fakta>(
    beskrivelse = "Krav til minsteinntekt etter midlertidig korona-endret § 4-4 første ledd bokstav a - minst 0,75 ganger grunnbeløp siste 12 måneder",
    identifikator = "Krav til minsteinntekt etter midlertidig korona-endret § 4-4 første ledd bokstav a",
    implementasjon = {
        when {
            arbeidsinntektSiste12 >= (grunnbeløp.times(0.75.toBigDecimal())) -> Evaluering.ja(
                "Inntekt siste 12 måneder er lik eller større enn 0,75 ganger grunnbeløp",
            )
            else -> Evaluering.nei("Inntekt siste 12 måneder er mindre enn 0,75 ganger grunnbeløp")
        }
    },
)

@Suppress("ktlint:standard:max-line-length")
internal val koronaOrdinærSiste36Måneder = Spesifikasjon<Fakta>(
    beskrivelse = "Krav til minsteinntekt etter midlertidig korona-endret § 4-4 første ledd bokstav b - minst 2,25 ganger grunnbeløpet siste 36 måneder",
    identifikator = "Krav til minsteinntekt etter midlertidig korona-endret § 4-4 første ledd bokstav b",
    implementasjon = {
        when {
            arbeidsinntektSiste36 >= (grunnbeløp.times(2.25.toBigDecimal())) -> Evaluering.ja(
                "Inntekt siste 36 måneder er lik eller større enn 2,25 ganger grunnbeløp",
            )
            else -> Evaluering.nei("Inntekt siste 36 måneder er mindre enn 2,25 ganger grunnbeløp")
        }
    },
)

@Suppress("ktlint:standard:max-line-length")
internal val koronaOrdinærSiste12MånederMedFangstOgFiske = Spesifikasjon<Fakta>(
    beskrivelse = "Krav til minsteinntekt etter § 4-18 + midlertidig korona-endret § 4-4 første ledd bokstav a - minst 0,75 ganger grunnbeløp siste 12 måneder",
    identifikator = "Krav til minsteinntekt etter § 4-18 + midlertidig korona-endret § 4-4 første ledd bokstav a",
    implementasjon = {
        when {
            erGyldigFangstOgFisk() && inntektSiste12inkludertFangstOgFiske >= (grunnbeløp.times(0.75.toBigDecimal())) -> Evaluering.ja(
                "Inntekt inkludert inntekt fra fangst og fisk siste 12 måneder er lik eller større enn 0,75 ganger grunnbeløp",
            )
            else -> Evaluering.nei(
                koronaFangstOgFiskAvslagBegrunnelse(antallOpptjeningsMåneder = "12", antallGangerGrunnbeløp = "0,75"),
            )
        }
    },
)

@Suppress("ktlint:standard:max-line-length")
internal val koronaOrdinærSiste36MånederMedFangstOgFiske = Spesifikasjon<Fakta>(
    beskrivelse = "Krav til minsteinntekt etter § 4-18 + midlertidig korona-endret § 4-4 første ledd bokstav b - minst 2,25 ganger grunnbeløp siste 36 måneder",
    identifikator = "Krav til minsteinntekt etter § 4-18 + midlertidig korona-endret § 4-4 første ledd bokstav b",
    implementasjon = {
        when {
            erGyldigFangstOgFisk() && inntektSiste36inkludertFangstOgFiske >= (grunnbeløp.times(2.25.toBigDecimal())) -> Evaluering.ja(
                "Inntekt inkludert inntekt fra fangst og fisk siste 36 måneder er lik eller større enn 2,25 ganger grunnbeløp",
            )
            else -> Evaluering.nei(
                koronaFangstOgFiskAvslagBegrunnelse(antallOpptjeningsMåneder = "36", antallGangerGrunnbeløp = "2,25"),
            )
        }
    },
)

@Suppress("ktlint:standard:max-line-length")
private fun Fakta.koronaFangstOgFiskAvslagBegrunnelse(antallOpptjeningsMåneder: String, antallGangerGrunnbeløp: String) =
    if (erGyldigFangstOgFisk()) {
        "Inntekt inkludert inntekt fra fangst og fisk siste $antallOpptjeningsMåneder måneder er mindre enn $antallGangerGrunnbeløp ganger grunnbeløp"
    } else {
        "Ikke gyldig fangst og fisk. Dette skyldes at regelverket avviklet inntekt fra fangst og fisk 01.01.2022"
    }

internal val koronaFangstOgFisk = koronaOrdinærSiste36MånederMedFangstOgFiske eller koronaOrdinærSiste12MånederMedFangstOgFiske

internal val koronaOrdinær: Spesifikasjon<Fakta> =
    (koronaOrdinærSiste12Måneder eller koronaOrdinærSiste36Måneder).eller(koronaFangstOgFisk)
        .med(
            identifikator = "Krav til minsteinntekt etter midlertidig korona-endret § 4-4",
            beskrivelse = "Krav til minsteinntekt etter ordinære regler",
        )

// https://lovdata.no/forskrift/2020-03-20-368/§2-6 - Unntak Folketrygdloven § 4-4 (krav til minsteinntekt)

internal val lærling = Spesifikasjon<Fakta>(
    beskrivelse = "§ 2-6.Midlertidig inntekssikringsordning for lærlinger – unntak fra folketrygdloven § 4-4",
    identifikator = "§ 2-6.Midlertidig inntekssikringsordning for lærlinger",
    implementasjon = {
        when {
            lærling -> Evaluering.ja("Lærling gis unntak fra unntak fra folketrygdloven § 4-4 ")
            else -> Evaluering.nei("Kun lærling gis unntak fra folketrygdloven § 4-4")
        }
    },
)
