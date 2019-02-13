package no.nav.dagpenger.regel.minsteinntekt

import org.json.JSONException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class PeriodeSubsumsjonsBehovTest {

    val emptyjsonBehov = """
            {}
            """.trimIndent()
    val emptyjsonObject = JsonDeserializer().deserialize(null, emptyjsonBehov.toByteArray())!!
    val emptysubsumsjonsBehov = SubsumsjonsBehov(emptyjsonObject)

    val jsonBehovMedInntekt = """
            {
                "inntekt": {"inntektsId": "", "inntekt": 0}
            }
            """.trimIndent()
    val jsonObjectMedInntekt = JsonDeserializer().deserialize(null, jsonBehovMedInntekt.toByteArray())!!
    val subsumsjonsBehovMedInntekt = SubsumsjonsBehov(jsonObjectMedInntekt)

    val jsonBehovMedMinsteinntektResultat = """
            {
                "minsteinntektResultat": {}
            }
            """.trimIndent()
    val jsonObjectMedMinsteinntektResultat = JsonDeserializer().deserialize(null, jsonBehovMedMinsteinntektResultat.toByteArray())!!
    val subsumsjonsBehovMedMinsteinntektResultat = SubsumsjonsBehov(jsonObjectMedMinsteinntektResultat)

    val jsonBehovMedHentInntektTask = """
            {
                "tasks": ["hentInntekt"]
            }
            """.trimIndent()
    val jsonObjectMedHentInntektTask = JsonDeserializer().deserialize(null, jsonBehovMedHentInntektTask.toByteArray())!!
    val subsumsjonsBehovMedHentInntektTask = SubsumsjonsBehov(jsonObjectMedHentInntektTask)

    val jsonBehovMedAnnenTask = """
            {
                "tasks": ["annen task"]
            }
            """.trimIndent()
    val jsonObjectMedAnnenTask = JsonDeserializer().deserialize(null, jsonBehovMedAnnenTask.toByteArray())!!
    val subsumsjonsBehovAnnentTask = SubsumsjonsBehov(jsonObjectMedAnnenTask)

    val jsonBehovMedFlereTasks = """
            {
                "tasks": ["annen task", "hentInntekt"]
            }
            """.trimIndent()
    val jsonObjectMedFlereTasks = JsonDeserializer().deserialize(null, jsonBehovMedFlereTasks.toByteArray())!!
    val subsumsjonsBehovFleretTasks = SubsumsjonsBehov(jsonObjectMedFlereTasks)

    val jsonBehovMedInntektogMinsteinntektResultat = """
            {
                "inntekt": 0,
                "minsteinntektResultat": {}
            }
            """.trimIndent()
    val jsonObjectMedInntektogMinsteinntektResultat = JsonDeserializer().deserialize(null, jsonBehovMedInntektogMinsteinntektResultat.toByteArray())!!
    val subsumsjonsBehovMedInntektogMinsteinntektResultat = SubsumsjonsBehov(jsonObjectMedInntektogMinsteinntektResultat)

    val jsonBehovMedInntektogHentInntektTask = """
            {
                "inntekt": {"inntektsId": "", "inntekt": 0},
                "tasks": ["hentInntekt"]
            }
            """.trimIndent()
    val jsonObjectMedInntektogHentInntektTask = JsonDeserializer().deserialize(null, jsonBehovMedInntektogHentInntektTask.toByteArray())!!
    val subsumsjonsBehovMedInntektogHentInntektTask = SubsumsjonsBehov(jsonObjectMedInntektogHentInntektTask)

    val jsonBehovMedVernepliktTrue = """
            {
                "avtjentVerneplikt": true
            }
            """.trimIndent()
    val jsonObjectMedVernepliktTrue = JsonDeserializer().deserialize(null, jsonBehovMedVernepliktTrue.toByteArray())!!
    val subsumsjonsBehovmedVernepliktTrue = SubsumsjonsBehov(jsonObjectMedVernepliktTrue)

    val jsonBehovMedVernepliktFalse = """
            {
                "avtjentVerneplikt": false
            }
            """.trimIndent()
    val jsonObjectMedVernepliktFalse = JsonDeserializer().deserialize(null, jsonBehovMedVernepliktFalse.toByteArray())!!
    val subsumsjonsBehovmedVernepliktFalse = SubsumsjonsBehov(jsonObjectMedVernepliktFalse)

    @Test
    fun ` Should need hentInntektsTask when there is no hentInntektsTask and no inntekt `() {

        assert(emptysubsumsjonsBehov.needsHentInntektsTask())
        Assertions.assertFalse(subsumsjonsBehovMedInntekt.needsHentInntektsTask())
        Assertions.assertFalse(subsumsjonsBehovMedHentInntektTask.needsHentInntektsTask())
        Assertions.assertFalse(subsumsjonsBehovMedInntektogHentInntektTask.needsHentInntektsTask())
    }

    @Test
    fun ` Should need minsteinntektResultat when there is inntekt and no minsteinntektResultat `() {

        assert(subsumsjonsBehovMedInntekt.needsMinsteinntektResultat())
        Assertions.assertFalse(emptysubsumsjonsBehov.needsMinsteinntektResultat())
        Assertions.assertFalse(subsumsjonsBehovMedInntektogMinsteinntektResultat.needsMinsteinntektResultat())
        Assertions.assertFalse(subsumsjonsBehovMedMinsteinntektResultat.needsMinsteinntektResultat())
    }

    @Test
    fun ` Should have minsteinntektResultat when it has minsteinntektResultat `() {

        assert(subsumsjonsBehovMedMinsteinntektResultat.hasMinsteinntektResultat())
        Assertions.assertFalse(emptysubsumsjonsBehov.hasMinsteinntektResultat())
    }

    @Test
    fun ` Should have inntekt when it has inntekt `() {

        assert(subsumsjonsBehovMedInntekt.hasInntekt())
        Assertions.assertFalse(emptysubsumsjonsBehov.hasInntekt())
    }

    @Test
    fun ` Should have hentInntektTask when it has hentInntektTask `() {

        assert(subsumsjonsBehovMedHentInntektTask.hasHentInntektTask())
        assert(subsumsjonsBehovFleretTasks.hasHentInntektTask())
        Assertions.assertFalse(emptysubsumsjonsBehov.hasHentInntektTask())
        Assertions.assertFalse(subsumsjonsBehovAnnentTask.hasHentInntektTask())
    }

    @Test
    fun ` Should have tasks when it has tasks `() {

        assert(subsumsjonsBehovMedHentInntektTask.hasTasks())
        assert(subsumsjonsBehovAnnentTask.hasTasks())
        assert(subsumsjonsBehovFleretTasks.hasTasks())
        Assertions.assertFalse(emptysubsumsjonsBehov.hasTasks())
    }

    @Test
    fun ` Should be able to add tasks `() {
        val subsumsjonsBehov = emptysubsumsjonsBehov

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

        assert(subsumsjonsBehovmedVernepliktTrue.hasVerneplikt())
        Assertions.assertFalse(subsumsjonsBehovmedVernepliktFalse.hasVerneplikt())
        Assertions.assertFalse(emptysubsumsjonsBehov.hasVerneplikt())
    }

    @Test
    fun ` Should be able to add minsteinntektResultat `() {
        val subsumsjonsBehov = emptysubsumsjonsBehov

        Assertions.assertFalse(subsumsjonsBehov.hasMinsteinntektResultat())

        val periodeSubsumsjon = MinsteinntektResultat("123", "456", "REGEL", true)
        subsumsjonsBehov.addMinsteinntektResultat(periodeSubsumsjon)

        assert(subsumsjonsBehov.hasMinsteinntektResultat())
    }

    @Test
    fun ` Should be able to return inntekt `() {

        assertEquals(0, subsumsjonsBehovMedInntekt.getInntekt().inntektValue)
        assertThrows<JSONException> { emptysubsumsjonsBehov.getInntekt() }
    }
}