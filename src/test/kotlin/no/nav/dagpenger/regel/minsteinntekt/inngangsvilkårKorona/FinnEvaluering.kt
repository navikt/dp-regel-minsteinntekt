package no.nav.dagpenger.regel.minsteinntekt.inngangsvilkårKorona

import no.nav.nare.core.evaluations.Evaluering

fun finnEvaluering(evaluering: Evaluering, string: String): Evaluering? {
    if (evaluering.identifikator == string) {
        return evaluering
    }
    evaluering.children.forEach {
        if (finnEvaluering(it, string) != null) {
            return it
        }
    }
    return null
}