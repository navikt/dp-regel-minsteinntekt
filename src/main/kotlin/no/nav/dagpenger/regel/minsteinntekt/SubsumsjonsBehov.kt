package no.nav.dagpenger.regel.minsteinntekt

import org.json.JSONObject

data class SubsumsjonsBehov(val jsonObject: JSONObject) {

    companion object {
        val MINSTEINNTEKT_RESULTAT = "minsteinntektResultat"
        val INNTEKT = "inntekt"
        val TASKS = "tasks"
        val TASKS_HENT_INNTEKT = "hentInntekt"
        val AVTJENT_VERNEPLIKT = "avtjentVerneplikt"
    }

    fun needsHentInntektsTask(): Boolean = !hasInntekt() && !hasHentInntektTask()

    fun needsMinsteinntektResultat(): Boolean = hasInntekt() && !hasMinsteinntektResultat()

    fun hasMinsteinntektResultat(): Boolean = jsonObject.has(MINSTEINNTEKT_RESULTAT)

    fun hasInntekt() = jsonObject.has(INNTEKT)

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

    fun addMinsteinntektResultat(minsteinntektResultat: MinsteinntektResultat) { jsonObject.put(MINSTEINNTEKT_RESULTAT, minsteinntektResultat.build()) }

    fun getInntekt(): Inntekt = Inntekt(jsonObject.get(INNTEKT) as JSONObject)

    class Builder {

        val jsonObject = JSONObject()

        fun inntekt(inntekt: Inntekt): Builder {
            jsonObject.put(INNTEKT, inntekt.build())
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

data class Inntekt(val inntektsId: String, val inntektValue: Int) {

    companion object {
        val INNTEKTSID = "inntektsId"
        val INNTEKT = "inntekt"
    }

    constructor(jsonObject: JSONObject):
        this(jsonObject.get(INNTEKTSID) as String, jsonObject.get(INNTEKT) as Int)

    fun build(): JSONObject {
        return JSONObject()
            .put(INNTEKTSID, inntektsId)
            .put(INNTEKT, inntektValue)
    }
}