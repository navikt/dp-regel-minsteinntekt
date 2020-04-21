package no.nav.dagpenger.regel.minsteinntekt

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.inntekt.v1.Inntekt

private val jsonAdapterInntekt = moshiInstance.adapter(Inntekt::class.java)

private val bruktInntektsPeriodeAdapter = moshiInstance.adapter<InntektsPeriode>(InntektsPeriode::class.java)

internal fun packetToFakta(packet: Packet): Fakta {
    val inntekt: Inntekt =
        packet.getObjectValue(Minsteinntekt.INNTEKT) { serialized -> checkNotNull(jsonAdapterInntekt.fromJsonValue(serialized)) }
    val avtjentVernePlikt = packet.getNullableBoolean(Minsteinntekt.AVTJENT_VERNEPLIKT) ?: false
    val bruktInntektsPeriode =
        packet.getNullableObjectValue(Minsteinntekt.BRUKT_INNTEKTSPERIODE, bruktInntektsPeriodeAdapter::fromJsonValue)
    val fangstOgFisk = packet.getNullableBoolean(Minsteinntekt.FANGST_OG_FISK) ?: false
    val beregningsDato = packet.getLocalDate(Minsteinntekt.BEREGNINGSDAGTO)
    val lærling = packet.getNullableBoolean(Minsteinntekt.LÆRLING) ?: false

    return Fakta(
        inntekt = inntekt,
        bruktInntektsPeriode = bruktInntektsPeriode,
        verneplikt = avtjentVernePlikt,
        fangstOgFisk = fangstOgFisk,
        beregningsdato = beregningsDato,
        lærling = lærling
    )
}
