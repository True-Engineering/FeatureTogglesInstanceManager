package ru.trueengineering.featureflag.manager

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.InetAddress
import java.net.UnknownHostException


@ConditionalOnProperty("management.metrics.export.influx.enabled")
@ConditionalOnClass(MeterRegistryCustomizer::class)
@Configuration
class MicrometerConfiguration {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${spring.application.name}")
    private val appName: String? = null

    @Value("\${DATA_CENTER_NAME:}")
    private val dc: String? = null

    @Value("\${HOSTNAME:}")
    private val hostName: String? = null

    @Value("\${APP_VERSION:local}")
    private val version: String? = null

    @Bean
    fun metricsCommonTags(): MeterRegistryCustomizer<MeterRegistry>? {
        val host = if (hostName!!.isEmpty()) getHostName() else hostName
        val dataCenter = if (dc!!.isEmpty()) getHostName() else dc
        return MeterRegistryCustomizer { registry: MeterRegistry ->
            log.info("Registry metrics for dc: {} app: {} version: {} host: {}", dataCenter, appName, version, host)
            registry.config()
                    .commonTags("host", host, "app", appName, "dc", dataCenter, "version", version)
        }
    }

    private fun getHostName(): String {
        var localHost: InetAddress? = null
        try {
            localHost = InetAddress.getLocalHost()
        } catch (e: UnknownHostException) {
            log.error("Unable to get hostName")
        }
        return if (localHost != null) {
            localHost.hostName
        } else {
            "unknown"
        }
    }
}