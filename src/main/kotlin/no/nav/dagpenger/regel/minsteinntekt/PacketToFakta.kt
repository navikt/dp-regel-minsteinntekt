package no.nav.dagpenger.regel.minsteinntekt

import com.fasterxml.jackson.databind.JsonNode
import java.math.BigDecimal
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.events.inntekt.v1.InntektKlasse
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.events.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.minsteinntekt.Application.Companion.BEREGNINGSDATO_GAMMEL_SKRIVEMÅTE
import no.nav.dagpenger.regel.minsteinntekt.LøsningService.Companion.BEREGNINGSDATO_NY_SRKIVEMÅTE
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.BRUKT_INNTEKTSPERIODE
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.FANGST_OG_FISK
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.INNTEKT
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.LÆRLING
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asYearMonth

private val jsonAdapterInntekt = moshiInstance.adapter(Inntekt::class.java)

private val bruktInntektsPeriodeAdapter = moshiInstance.adapter<InntektsPeriode>(InntektsPeriode::class.java)

internal fun packetToFakta(packet: Packet): Fakta {
    val inntekt: Inntekt =
        packet.getObjectValue(INNTEKT) { serialized ->
            checkNotNull(
                jsonAdapterInntekt.fromJsonValue(
                    serialized
                )
            )
        }
    val avtjentVernePlikt = packet.getNullableBoolean(AVTJENT_VERNEPLIKT) ?: false
    val bruktInntektsPeriode =
        packet.getNullableObjectValue(BRUKT_INNTEKTSPERIODE, bruktInntektsPeriodeAdapter::fromJsonValue)
    val fangstOgFisk = packet.getNullableBoolean(FANGST_OG_FISK) ?: false
    val beregningsDato = packet.getLocalDate(BEREGNINGSDATO_GAMMEL_SKRIVEMÅTE)
    val lærling = packet.getNullableBoolean(LÆRLING) ?: false

    return Fakta(
        inntekt = inntekt,
        bruktInntektsPeriode = bruktInntektsPeriode,
        verneplikt = avtjentVernePlikt,
        fangstOgFisk = fangstOgFisk,
        beregningsdato = beregningsDato,
        lærling = lærling
    )
}

internal fun JsonMessage.toFakta(): Fakta {
    val inntekt = this[INNTEKT].let {
        Inntekt(
            inntektsId = it["inntektsId"].asText(),
            inntektsListe = it["inntektsListe"].map {
                KlassifisertInntektMåned(
                    årMåned = it["årMåned"].asYearMonth(),
                    klassifiserteInntekter = it["klassifiserteInntekter"].map {
                        KlassifisertInntekt(
                            beløp = BigDecimal(
                                it["beløp"].asInt()
                            ), inntektKlasse = InntektKlasse.valueOf(it["inntektKlasse"].asText())
                        )
                    })
            }, manueltRedigert = it["manueltRedigert"].asBoolean(),
            sisteAvsluttendeKalenderMåned = it["sisteAvsluttendeKalenderMåned"].asYearMonth()
        )
    }
    val avtjentVerneplikt = this[AVTJENT_VERNEPLIKT].asBoolean(false)
    val bruktInntektsPeriode =
        this["bruktInntektsPeriode"].takeIf(JsonNode::isObject)
            ?.let { InntektsPeriode(it["førsteMåned"].asYearMonth(), it["sisteMåned"].asYearMonth()) }
    val fangstOgFisk = this["oppfyllerKravTilFangstOgFisk"].asBoolean(false)
    val beregningsDato = this[BEREGNINGSDATO_NY_SRKIVEMÅTE].asLocalDate()
    val lærling = this[LÆRLING].asBoolean(false)

    return Fakta(
        inntekt = inntekt,
        bruktInntektsPeriode = bruktInntektsPeriode,
        verneplikt = avtjentVerneplikt,
        fangstOgFisk = fangstOgFisk,
        beregningsdato = beregningsDato,
        lærling = lærling
    )
}
