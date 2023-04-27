package no.nav.dagpenger.regel.minsteinntekt

import io.getunleash.DefaultUnleash
import io.getunleash.strategy.Strategy
import io.getunleash.util.UnleashConfig

const val GJUSTERING_TEST = "dp-g-justeringstest"

fun setupUnleash(unleashApiUrl: String): DefaultUnleash {
    val appName = "dp-regel-minsteinntekt"
    val unleashconfig = UnleashConfig.builder()
        .appName(appName)
        .instanceId(appName)
        .unleashAPI(unleashApiUrl)
        .build()

    return DefaultUnleash(unleashconfig, ByClusterStrategy(Cluster.current))
}

class ByClusterStrategy(private val currentCluster: Cluster) : Strategy {
    override fun getName(): String = "byCluster"
    override fun isEnabled(parameters: MutableMap<String, String>): Boolean {
        val clustersParameter = parameters?.get("cluster") ?: return false
        val alleClustere = clustersParameter.split(",").map { it.trim() }.map { it.lowercase() }.toList()
        return alleClustere.contains(currentCluster.asString())
    }
}

enum class Cluster {
    DEV_FSS, PROD_FSS, ANNET;

    companion object {
        val current: Cluster by lazy {
            when (System.getenv("NAIS_CLUSTER_NAME")) {
                "dev-fss" -> DEV_FSS
                "prod-fss" -> PROD_FSS
                else -> ANNET
            }
        }
    }

    fun asString(): String = name.lowercase().replace("_", "-")
}
