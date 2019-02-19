package no.nav.dagpenger.regel.minsteinntekt

import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class MinsteinntektSubsumsjonsBehovTest {

    fun jsonToBehov(json: String): SubsumsjonsBehov =
        SubsumsjonsBehov(JsonDeserializer().deserialize("", json.toByteArray()) ?: JSONObject())

    val emptyjsonBehov = """
            {}
            """.trimIndent()

    val jsonBehovMedInntekt = """
            {
                "inntekt": {"inntektsId": "", "inntekt": 0}
            }
            """.trimIndent()

    val jsonBehovMedMinsteinntektResultat = """
            {
                "minsteinntektResultat": {}
            }
            """.trimIndent()

    val jsonBehovMedHentInntektTask = """
            {
                "tasks": ["hentInntekt"]
            }
            """.trimIndent()

    val jsonBehovMedAnnenTask = """
            {
                "tasks": ["annen task"]
            }
            """.trimIndent()

    val jsonBehovMedFlereTasks = """
            {
                "tasks": ["annen task", "hentInntekt"]
            }
            """.trimIndent()

    val jsonBehovMedInntektogMinsteinntektResultat = """
            {
                "inntekt": 0,
                "minsteinntektResultat": {}
            }
            """.trimIndent()

    val jsonBehovMedInntektogHentInntektTask = """
            {
                "inntekt": {"inntektsId": "", "inntekt": 0},
                "tasks": ["hentInntekt"]
            }
            """.trimIndent()

    val jsonBehovMedVernepliktTrue = """
            {
                "harAvtjentVerneplikt": true
            }
            """.trimIndent()

    val jsonBehovMedVernepliktFalse = """
            {
                "harAvtjentVerneplikt": false
            }
            """.trimIndent()

    @Test
    fun ` Should need hentInntektsTask when there is no hentInntektsTask and no inntekt `() {

        assert(jsonToBehov(emptyjsonBehov).needsHentInntektsTask())
        Assertions.assertFalse(jsonToBehov(jsonBehovMedInntekt).needsHentInntektsTask())
        Assertions.assertFalse(jsonToBehov(jsonBehovMedHentInntektTask).needsHentInntektsTask())
        Assertions.assertFalse(jsonToBehov(jsonBehovMedInntektogHentInntektTask).needsHentInntektsTask())
    }

    @Test
    fun ` Should need minsteinntektResultat when there is inntekt and no minsteinntektResultat `() {

        assert(jsonToBehov(jsonBehovMedInntekt).needsMinsteinntektResultat())
        Assertions.assertFalse(jsonToBehov(emptyjsonBehov).needsMinsteinntektResultat())
        Assertions.assertFalse(jsonToBehov(jsonBehovMedInntektogMinsteinntektResultat).needsMinsteinntektResultat())
        Assertions.assertFalse(jsonToBehov(jsonBehovMedMinsteinntektResultat).needsMinsteinntektResultat())
    }

    @Test
    fun ` Should have minsteinntektResultat when it has minsteinntektResultat `() {

        assert(jsonToBehov(jsonBehovMedMinsteinntektResultat).hasMinsteinntektResultat())
        Assertions.assertFalse(jsonToBehov(emptyjsonBehov).hasMinsteinntektResultat())
    }

    @Test
    fun ` Should have inntekt when it has inntekt `() {

        assert(jsonToBehov(jsonBehovMedInntekt).hasInntekt())
        Assertions.assertFalse(jsonToBehov(emptyjsonBehov).hasInntekt())
    }

    @Test
    fun ` Should have hentInntektTask when it has hentInntektTask `() {

        assert(jsonToBehov(jsonBehovMedHentInntektTask).hasHentInntektTask())
        assert(jsonToBehov(jsonBehovMedFlereTasks).hasHentInntektTask())
        Assertions.assertFalse(jsonToBehov(emptyjsonBehov).hasHentInntektTask())
        Assertions.assertFalse(jsonToBehov(jsonBehovMedAnnenTask).hasHentInntektTask())
    }

    @Test
    fun ` Should have tasks when it has tasks `() {

        assert(jsonToBehov(jsonBehovMedHentInntektTask).hasTasks())
        assert(jsonToBehov(jsonBehovMedAnnenTask).hasTasks())
        assert(jsonToBehov(jsonBehovMedFlereTasks).hasTasks())
        Assertions.assertFalse(jsonToBehov(emptyjsonBehov).hasTasks())
    }

    @Test
    fun ` Should be able to add tasks `() {
        val subsumsjonsBehov = jsonToBehov(emptyjsonBehov)

        Assertions.assertFalse(subsumsjonsBehov.hasTasks())

        subsumsjonsBehov.addTask("Annen Task")

        assert(subsumsjonsBehov.hasTasks())
        Assertions.assertFalse(subsumsjonsBehov.hasHentInntektTask())

        subsumsjonsBehov.addTask("hentInntekt")

        assert(subsumsjonsBehov.hasTasks())
        assert(subsumsjonsBehov.hasHentInntektTask())
    }

    @Test
    fun ` Should be able to return verneplikt `() {

        assert(jsonToBehov(jsonBehovMedVernepliktTrue).hasVerneplikt())
        Assertions.assertFalse(jsonToBehov(jsonBehovMedVernepliktFalse).hasVerneplikt())
        Assertions.assertFalse(jsonToBehov(emptyjsonBehov).hasVerneplikt())
    }

    @Test
    fun ` Should be able to add minsteinntektResultat `() {
        val subsumsjonsBehov = jsonToBehov(emptyjsonBehov)

        Assertions.assertFalse(subsumsjonsBehov.hasMinsteinntektResultat())

        val periodeSubsumsjon = MinsteinntektResultat("123", "456", "REGEL", true)
        subsumsjonsBehov.addMinsteinntektResultat(periodeSubsumsjon)

        assert(subsumsjonsBehov.hasMinsteinntektResultat())
    }

    @Test
    fun ` Should be able to return inntekt `() {

        assertEquals(0, jsonToBehov(jsonBehovMedInntekt).getInntekt().inntektValue)
        assertThrows<JSONException> { jsonToBehov(emptyjsonBehov).getInntekt() }
    }
}