package no.nav.dagpenger.regel.minsteinntekt

import org.json.JSONObject

data class SubsumsjonsBehov(val jsonObject: JSONObject) {

    fun needsHentInntektsTask(): Boolean = !hasInntekt() && !hasHentInntektTask()

    fun needsMinsteinntektSubsumsjon(): Boolean = hasInntekt() && !hasMinsteinntektSubsumsjon()

    fun hasMinsteinntektSubsumsjon(): Boolean = jsonObject.has("minsteinntektSubsumsjon")

    private fun hasInntekt() = jsonObject.has("inntekt")

    private fun hasHentInntektTask(): Boolean {
        if (jsonObject.has("tasks")) {
            val tasks = jsonObject.getJSONArray("tasks")
            for (task in tasks) {
                if (task.toString() == "hentInntekt") {
                    return true
                }
            }
        }
        return false
    }

    fun hasTasks(): Boolean = jsonObject.has("tasks")

    fun addTask(task: String) {
        if (hasTasks()) {
            jsonObject.append("tasks", task)
        } else {
            jsonObject.put("tasks", listOf(task))
        }
    }

    fun hasVerneplikt(): Boolean = if (jsonObject.has("avtjentVerneplikt")) jsonObject.getBoolean("avtjentVerneplikt") else false

    fun addMinsteinntektSubsumsjon(minsteinntektSubsumsjon: MinsteinntektSubsumsjon) { jsonObject.put("minsteinntektSubsumsjon", minsteinntektSubsumsjon.build()) }

    fun getInntekt(): Int = jsonObject.get("inntekt") as Int

    data class MinsteinntektSubsumsjon(val sporingsId: String, val subsumsjonsId: String, val regelidentifikator: String, val oppfyllerMinsteinntekt: Boolean) {

        fun build(): JSONObject {
            return JSONObject()
                .put("sporingsId", sporingsId)
                .put("subsumsjonsId", subsumsjonsId)
                .put("regelIdentifikator", regelidentifikator)
                .put("oppfyllerMinsteinntekt", oppfyllerMinsteinntekt)
        }
    }

    class Builder {

        val jsonObject = JSONObject()

        fun vedtaksId(vedtaktsId: String): Builder {
            jsonObject.put("vedtaksId", vedtaktsId)
            return this
        }

        fun aktorId(aktorId: String): Builder {
            jsonObject.put("aktorId", aktorId)
            return this
        }

        fun beregningsDato(beregningDato: String): Builder {
            jsonObject.put("beregningsDato", beregningDato)
            return this
        }

        fun inntekt(inntekt: Int): Builder {
            jsonObject.put("inntekt", inntekt)
            return this
        }

        fun task(tasks: List<String>): Builder {
            jsonObject.put("tasks", tasks)
            return this
        }

        fun minsteinntektSubsumsjon(minsteinntektSubsumsjon: MinsteinntektSubsumsjon): Builder {
            jsonObject.put("minsteinntektSubsumsjon", minsteinntektSubsumsjon.build())
            return this
        }

        fun build(): SubsumsjonsBehov = SubsumsjonsBehov(jsonObject)
    }
}