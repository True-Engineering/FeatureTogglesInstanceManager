package ru.trueengineering.featureflag.manager.core.utils

import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import ru.trueengineering.featureflag.manager.core.domen.toggle.EnvironmentFeatureFlag


object HashUtils {
    private val log = LoggerFactory.getLogger(javaClass)

    fun getHash(string: String): String {
        return DigestUtils.sha256Hex(string)
    }

    fun getHash(features: List<EnvironmentFeatureFlag>): String {
        val sortedFlags = features.sortedWith(Comparator.comparing(EnvironmentFeatureFlag::uid))
        val hash = getHash(sortedFlags.toString())
        log.trace("hash of {} = {}", features, hash)
        return hash
    }

}