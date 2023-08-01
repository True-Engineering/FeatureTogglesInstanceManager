package ru.trueengineering.featureflag.manager.ports.rest.mdc

import org.slf4j.MDC
import java.io.Closeable

open class MDCProperty(private val name: String, private val value: Any) : Closeable {

    init {
        MDC.put(name, value.toString())
    }

    override fun close() {
        MDC.remove(name)
    }
}

class MDCProperties(private val mdcProperties: List<MDCProperty>) : Closeable {
    override fun close() {
        for (mdcProperty in mdcProperties) {
            mdcProperty.close()
        }
    }
}