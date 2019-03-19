package no.nav.dagpenger.regel.minsteinntekt

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class InngångsvilkårTest {

    @Test
    fun `Inngångsvilkår består av Rett § 4-19 Dagpenger etter avtjent verneplikt  og § 4-4 minsteinntekt `() {
        assertEquals("ORDINÆR, VERNEPLIKT", inngangsVilkår.children.joinToString { it.identitet })
    }

    @Test
    fun ` Minsteinntekt ordinær består § 4-4 minsteinntekt `() {
        assertEquals(
            "ORDINÆR_12 ELLER ORDINÆR_36, ORDINÆR_12_NÆRING ELLER ORDINÆR_36_NÆRING",
            ordninær.children.joinToString { it.identitet })
    }
}