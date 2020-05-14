package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt
import no.nav.dagpenger.regel.minsteinntekt.Application.Companion.BEREGNINGSDATO_GAMMEL_SKRIVEMÅTE
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.AVTJENT_VERNEPLIKT
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.BRUKT_INNTEKTSPERIODE
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.FANGST_OG_FISK
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.INNTEKT
import no.nav.dagpenger.regel.minsteinntekt.Minsteinntekt.Companion.LÆRLING

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
