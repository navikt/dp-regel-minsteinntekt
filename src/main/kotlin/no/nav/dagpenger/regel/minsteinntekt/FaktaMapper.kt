package no.nav.dagpenger.regel.minsteinntekt

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.regel.minsteinntekt.FaktaMapper.avtjentVerneplikt
import no.nav.dagpenger.regel.minsteinntekt.FaktaMapper.beregningsdato
import no.nav.dagpenger.regel.minsteinntekt.FaktaMapper.bruktInntektsperiode
import no.nav.dagpenger.regel.minsteinntekt.FaktaMapper.fangstOgFiske
import no.nav.dagpenger.regel.minsteinntekt.FaktaMapper.inntekt
import no.nav.dagpenger.regel.minsteinntekt.FaktaMapper.lærling
import no.nav.dagpenger.regel.minsteinntekt.FaktaMapper.regelverksdato
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.BRUKT_INNTEKTSPERIODE
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.FANGST_OG_FISKE
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.INNTEKT
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.LÆRLING
import no.nav.dagpenger.regel.minsteinntekt.MinsteinntektBehovløser.Companion.REGELVERKSDATO
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.isMissingOrNull

private val sikkerLogg = KotlinLogging.logger("tjenestekall")

internal fun packetToFakta(
    packet: JsonMessage,
    grunnbeløpStrategy: GrunnbeløpStrategy,
): Fakta {
    val beregningsdato = packet.beregningsdato()
    val regelverksdato = packet.regelverksdato()
    val avtjentVerneplikt = packet.avtjentVerneplikt()
    val fangstOgFiske = packet.fangstOgFiske()
    val lærling = packet.lærling()
    val bruktInntektsperiode = packet.bruktInntektsperiode()
    val inntekt: Inntekt = packet.inntekt()
    return Fakta(
        inntekt = inntekt,
        bruktInntektsperiode = bruktInntektsperiode,
        verneplikt = avtjentVerneplikt,
        fangstOgFiske = fangstOgFiske,
        beregningsdato = beregningsdato,
        regelverksdato = regelverksdato,
        lærling = lærling,
        grunnbeløp = grunnbeløpStrategy.grunnbeløp(beregningsdato),
    )
}

object FaktaMapper {
    fun JsonMessage.beregningsdato() = this[BEREGNINGSDATO].asLocalDate()

    fun JsonMessage.regelverksdato() =
        when (this.harVerdi(REGELVERKSDATO)) {
            true -> this[REGELVERKSDATO].asLocalDate()
            false -> this.beregningsdato()
        }
    fun JsonMessage.fangstOgFiske() =
        when (this.harVerdi(FANGST_OG_FISKE)) {
            true -> this[FANGST_OG_FISKE].toBooleanStrict()
            false -> false
        }

    fun JsonMessage.lærling() =
        when (this.harVerdi(LÆRLING)) {
            true -> this[LÆRLING].toBooleanStrict()
            false -> false
        }
    fun JsonMessage.avtjentVerneplikt() =
        when (this.harVerdi(AVTJENT_VERNEPLIKT)) {
            true -> this[AVTJENT_VERNEPLIKT].toBooleanStrict()
            false -> false
        }
    fun JsonMessage.bruktInntektsperiode(): InntektsPeriode? {
        val bruktInntektsperiode = this[BRUKT_INNTEKTSPERIODE]
        return when (this.harVerdi(BRUKT_INNTEKTSPERIODE)) {
            true -> jsonMapper.convertValue(bruktInntektsperiode, InntektsPeriode::class.java)
            false -> null
        }
    }
    fun JsonMessage.inntekt(): Inntekt {
        val inntekt = this[INNTEKT]
        return when (this.harVerdi(INNTEKT)) {
            true -> jsonMapper.convertValue(inntekt, Inntekt::class.java)
            false -> throw ManglendeInntektException()
        }
    }
    class ManglendeInntektException : RuntimeException("Mangler inntekt")
    private fun JsonNode.toBooleanStrict() = this.asText().toBooleanStrict()
    private fun JsonMessage.harVerdi(field: String) = !this[field].isMissingOrNull()
}
