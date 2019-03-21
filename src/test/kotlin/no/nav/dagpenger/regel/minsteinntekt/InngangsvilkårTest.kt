package no.nav.dagpenger.regel.minsteinntekt

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class InngangsvilkårTest {

    @Test
    fun `Inngångsvilkår består av Rett § 4-19 Dagpenger etter avtjent verneplikt  og § 4-4 minsteinntekt `() {
        assertEquals("§ 4-4 § 4-18, § 4-19", inngangsVilkår.children.joinToString { it.identitet })
    }

    @Test
    fun ` Minsteinntekt ordinær består § 4-4 minsteinntekt `() {
        assertEquals(
            "§ 4-4 12mnd ELLER § 4-4 36mnd, § 4-18 12mnd ELLER § 4-18 36mnd",
            ordinær.children.joinToString { it.identitet })
    }
}