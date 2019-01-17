package no.nav.dagpenger.regel.minsteinntekt

data class Environment(
    val username: String = getEnvVar("SRVDP_REGEL_MINSTEINNTEKT_USERNAME"),
    val password: String = getEnvVar("SRVDP_REGEL_MINSTEINNTEKT_PASSWORD"),
    val bootstrapServersUrl: String = getEnvVar("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
    val schemaRegistryUrl: String = getEnvVar("KAFKA_SCHEMA_REGISTRY_URL", "http://localhost:8081"),
    val fasitEnvironmentName: String = getEnvVar(
        "FASIT_ENVIRONMENT_NAME",
        ""
    ),
    val httpPort: Int? = null
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
