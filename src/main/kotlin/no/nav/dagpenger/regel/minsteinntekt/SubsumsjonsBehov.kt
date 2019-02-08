package no.nav.dagpenger.regel.minsteinntekt

import org.json.JSONObject

data class SubsumsjonsBehov(val jsonObject: JSONObject) {

    companion object {
        val MINSTEINNTEKT_RESULTAT = "minsteinntektResultat"
        val INNTEKT = "inntekt"
        val TASKS = "tasks"
        val TASKS_HENT_INNTEKT = "hentInntekt"
        val AVTJENT_VERNEPLIKT = "avtjentVerneplikt"

        val SPORINGSID = "sporingsId"
        val SUBSUMSJONSID = "subsumsjonsId"
        val REGELIDENTIFIKATOR = "regelIdentifikator"
        val OPPFYLLER_MINSTEINNTEKT = "oppfyllerMinsteinntekt"
    }

    fun needsHentInntektsTask(): Boolean = !hasInntekt() && !hasHentInntektTask()

    fun needsMinsteinntektSubsumsjon(): Boolean = hasInntekt() && !hasMinsteinntektSubsumsjon()

    fun hasMinsteinntektSubsumsjon(): Boolean = jsonObject.has(MINSTEINNTEKT_RESULTAT )

    private fun hasInntekt() = jsonObject.has(INNTEKT)

    private fun hasHentInntektTask(): Boolean {
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

    fun addMinsteinntektSubsumsjon(minsteinntektSubsumsjon: MinsteinntektSubsumsjon) { jsonObject.put(MINSTEINNTEKT_RESULTAT , minsteinntektSubsumsjon.build()) }

    fun getInntekt(): Int = jsonObject.get(INNTEKT) as Int

    data class MinsteinntektSubsumsjon(val sporingsId: String, val subsumsjonsId: String, val regelidentifikator: String, val oppfyllerMinsteinntekt: Boolean) {

        fun build(): JSONObject {
            return JSONObject()
                .put(SPORINGSID, sporingsId)
                .put(SUBSUMSJONSID, subsumsjonsId)
                .put(REGELIDENTIFIKATOR, regelidentifikator)
                .put(OPPFYLLER_MINSTEINNTEKT, oppfyllerMinsteinntekt)
        }
    }

    class Builder {

        val jsonObject = JSONObject()

        fun inntekt(inntekt: Int): Builder {
            jsonObject.put(INNTEKT, inntekt)
            return this
        }

        fun task(tasks: List<String>): Builder {
            jsonObject.put(TASKS, tasks)
            return this
        }

        fun minsteinntektSubsumsjon(minsteinntektSubsumsjon: MinsteinntektSubsumsjon): Builder {
            jsonObject.put(MINSTEINNTEKT_RESULTAT,  minsteinntektSubsumsjon.build())
            return this
        }

        fun build(): SubsumsjonsBehov = SubsumsjonsBehov(jsonObject)
    }
}