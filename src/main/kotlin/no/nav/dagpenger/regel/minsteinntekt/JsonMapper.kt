package no.nav.dagpenger.regel.minsteinntekt

import tools.jackson.databind.DeserializationFeature
import tools.jackson.module.kotlin.jacksonMapperBuilder

// java.time-støtte (JavaTimeModule) er innebygd i jackson-databind i Jackson 3, ikke lenger nødvendig å registrere.
// WRITE_DATES_AS_TIMESTAMPS har flyttet til DateTimeFeature og er allerede false som default i Jackson 3
// (datoer serialiseres som ISO-8601-strenger), så eksplisitt disable er ikke lenger nødvendig.
internal val jsonMapper =
    jacksonMapperBuilder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build()
