package no.nav.dagpenger.regel.minsteinntekt

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import mu.KotlinLogging
import no.nav.common.JAASCredential
import no.nav.common.KafkaEnvironment
import no.nav.common.embeddedutils.getAvailablePort
import no.nav.dagpenger.events.avro.Inntektsdata
import no.nav.dagpenger.events.avro.Regel
import no.nav.dagpenger.events.avro.RegelType
import no.nav.dagpenger.events.avro.Vilkår
import no.nav.dagpenger.streams.Topics
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.SaslConfigs
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.Properties
import java.util.Random
import java.util.UUID
import kotlin.test.assertEquals

class MinsteinntektRegelComponentTest {

    private val LOGGER = KotlinLogging.logger {}

    companion object {
        private const val username = "srvkafkaclient"
        private const val password = "kafkaclient"

        val embeddedEnvironment = KafkaEnvironment(
            users = listOf(JAASCredential(username, password)),
            autoStart = false,
            withSchemaRegistry = true,
            withSecurity = true,
            topics = listOf(Topics.VILKÅR_EVENT.name)
        )

        val env = Environment(
            username = username,
            password = password,
            bootstrapServersUrl = embeddedEnvironment.brokersURL,
            schemaRegistryUrl = embeddedEnvironment.schemaRegistry!!.url,
            httpPort = getAvailablePort()
        )

        val minsteinntektRegel = MinsteinntektRegel(env)

        @BeforeAll
        @JvmStatic
        fun setup() {
            embeddedEnvironment.start()
            minsteinntektRegel.start()
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            minsteinntektRegel.stop()
            embeddedEnvironment.tearDown()
        }
    }

    @Test
    fun ` embedded kafka cluster is up and running `() {
        assertEquals(embeddedEnvironment.serverPark.status, KafkaEnvironment.ServerParkStatus.Started)
    }

    @Test
    fun ` Should be able to consume vilkår events and add results`() {

        val producer = vikårProducer(env)

        val consumer = vilkårConsumer(env)

        val record = producer.send(ProducerRecord(Topics.VILKÅR_EVENT.name, Vilkår.newBuilder().apply {
            id = UUID.randomUUID().toString()
            aktorId = Random().nextInt().toString()
            vedtaksId = Random().nextInt().toString()
            regler = listOf(Regel(RegelType.FIRE_FIRE, "12345", null, null))
            inntekter = Inntektsdata("", true, true, null)
        }.build())).get()
        LOGGER.info { "Produced -> ${record.topic()}  to offset ${record.offset()}" }

        Thread.sleep(5000)

        val vilkår = consumer.poll(Duration.ofSeconds(5)).toList()

        assertEquals(2, vilkår.size)
    }

    private fun vikårProducer(env: Environment): KafkaProducer<String, Vilkår> {

        return KafkaProducer(Properties().apply {
            put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, env.schemaRegistryUrl)
            put(ProducerConfig.CLIENT_ID_CONFIG, "vilkår-test-producer")
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, env.bootstrapServersUrl)
            put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                Topics.VILKÅR_EVENT.keySerde.serializer().javaClass.name
            )
            put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                Topics.VILKÅR_EVENT.valueSerde.serializer().javaClass.name
            )
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT")
            put(SaslConfigs.SASL_MECHANISM, "PLAIN")
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true)
            put(
                SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${env.username}\" password=\"${env.password}\";"
            )
        })
    }
    private fun vilkårConsumer(env: Environment): KafkaConsumer<String, Vilkår> {
        val consumer: KafkaConsumer<String, Vilkår> = KafkaConsumer(Properties().apply {
            put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, env.schemaRegistryUrl)
            put(ConsumerConfig.GROUP_ID_CONFIG, "vilkår-test-consumer")
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, env.bootstrapServersUrl)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                Topics.VILKÅR_EVENT.keySerde.deserializer().javaClass.name
            )
            put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                Topics.VILKÅR_EVENT.valueSerde.deserializer().javaClass.name
            )
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT")
            put(SaslConfigs.SASL_MECHANISM, "PLAIN")
            put(
                SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${env.username}\" password=\"${env.password}\";"
            )
        })

        consumer.subscribe(listOf(Topics.VILKÅR_EVENT.name))
        return consumer
    }
}
