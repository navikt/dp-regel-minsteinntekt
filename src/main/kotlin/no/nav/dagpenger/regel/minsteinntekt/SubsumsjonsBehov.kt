package no.nav.dagpenger.regel.minsteinntekt

import org.json.JSONObject
import java.math.BigDecimal
import java.time.YearMonth

data class SubsumsjonsBehov(val jsonObject: JSONObject) {

    companion object {
        val MINSTEINNTEKT_RESULTAT = "minsteinntektResultat"
        val INNTEKT = "inntektV1"
        val TASKS = "tasks"
        val TASKS_HENT_INNTEKT = "hentInntekt"
        val AVTJENT_VERNEPLIKT = "harAvtjentVerneplikt"
        val SENESTE_INNTEKTSMÅNED = "senesteInntektsmåned"
        val BRUKT_INNTEKTSPERIODE = "bruktInntektsPeriode"

        val jsonAdapterInntekt = moshiInstance.adapter(Inntekt::class.java)
        val jsonAdapterInntektsPeriode = moshiInstance.adapter(InntektsPeriode::class.java)
    }

    fun needsHentInntektsTask(): Boolean = !hasInntekt() && !hasHentInntektTask()

    fun needsMinsteinntektResultat(): Boolean = hasInntekt() && !hasMinsteinntektResultat()

    fun getSenesteInntektsmåned(): YearMonth = YearMonth.parse(jsonObject.get(SENESTE_INNTEKTSMÅNED).toString())

    fun hasBruktInntektsPeriode(): Boolean = jsonObject.has(BRUKT_INNTEKTSPERIODE)

    fun getBruktInntektsPeriode(): InntektsPeriode? {
        return if (hasBruktInntektsPeriode())
            jsonAdapterInntektsPeriode.fromJson(jsonObject.get(BRUKT_INNTEKTSPERIODE).toString())!!
        else null
    }

    fun hasHentInntektTask(): Boolean {
        if (jsonObject.has(TASKS)) {
            val tasks = jsonObject.getJSONArray(TASKS)
            for (task in tasks) {
                if (task.toString() == TASKS_HENT_INNTEKT) {
                    return true
                }
            }
        }
        return false
    }

    fun hasTasks(): Boolean = jsonObject.has(TASKS)

    fun addTask(task: String) {
        if (hasTasks()) {
            jsonObject.append(TASKS, task)
        } else {
            jsonObject.put(TASKS, listOf(task))
        }
    }

    fun hasVerneplikt(): Boolean = if (jsonObject.has(AVTJENT_VERNEPLIKT)) jsonObject.getBoolean(AVTJENT_VERNEPLIKT) else false

    fun hasMinsteinntektResultat(): Boolean = jsonObject.has(MINSTEINNTEKT_RESULTAT)

    fun addMinsteinntektResultat(minsteinntektResultat: MinsteinntektResultat) { jsonObject.put(MINSTEINNTEKT_RESULTAT, minsteinntektResultat.build()) }

    fun hasInntekt() = jsonObject.has(INNTEKT)

    fun getInntekt(): Inntekt = jsonAdapterInntekt.fromJson(jsonObject.get(INNTEKT).toString())!!

    class Builder {

        val jsonObject = JSONObject()

        fun inntekt(inntekt: Inntekt): Builder {
            val json = jsonAdapterInntekt.toJson(inntekt)
            jsonObject.put(INNTEKT, JSONObject(json))
            return this
        }

        fun bruktInntektsPeriode(bruktInntektsPeriode: InntektsPeriode): Builder {
            val json = jsonAdapterInntektsPeriode.toJson(bruktInntektsPeriode)
            jsonObject.put(BRUKT_INNTEKTSPERIODE, JSONObject(json))
            return this
        }

        fun senesteInntektsmåned(senesteInntektsmåned: YearMonth): Builder {
            jsonObject.put(SENESTE_INNTEKTSMÅNED, senesteInntektsmåned)
            return this
        }

        fun task(tasks: List<String>): Builder {
            jsonObject.put(TASKS, tasks)
            return this
        }

        fun minsteinntektResultat(minsteinntektResultat: MinsteinntektResultat): Builder {
            jsonObject.put(MINSTEINNTEKT_RESULTAT, minsteinntektResultat.build())
            return this
        }

        fun build(): SubsumsjonsBehov = SubsumsjonsBehov(jsonObject)
    }
}

data class MinsteinntektResultat(val sporingsId: String, val subsumsjonsId: String, val regelidentifikator: String, val oppfyllerMinsteinntekt: Boolean) {

    companion object {
        val SPORINGSID = "sporingsId"
        val SUBSUMSJONSID = "subsumsjonsId"
        val REGELIDENTIFIKATOR = "regelIdentifikator"
        val OPPFYLLER_MINSTEINNTEKT = "oppfyllerMinsteinntekt"
    }

    fun build(): JSONObject {
        return JSONObject()
            .put(SPORINGSID, sporingsId)
            .put(SUBSUMSJONSID, subsumsjonsId)
            .put(REGELIDENTIFIKATOR, regelidentifikator)
            .put(OPPFYLLER_MINSTEINNTEKT, oppfyllerMinsteinntekt)
    }
}

data class InntektsPeriode(
    val førsteMåned: YearMonth,
    val sisteMåned: YearMonth
)

data class Inntekt(
    val inntektsId: String,
    val inntektsListe: List<KlassifisertInntektMåned>
)

data class KlassifisertInntektMåned(
    val årMåned: YearMonth,
    val klassifiserteInntekter: List<KlassifisertInntekt>
)

data class KlassifisertInntekt(
    val beløp: BigDecimal,
    val inntektKlasse: InntektKlasse
)

enum class InntektKlasse {
    ARBEIDSINNTEKT,
    DAGPENGER,
    DAGPENGER_FANGST_FISKE,
    SYKEPENGER_FANGST_FISKE,
    NÆRINGSINNTEKT,
    SYKEPENGER,
    TILTAKSLØNN
}