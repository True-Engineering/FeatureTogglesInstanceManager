package ru.trueengineering.featureflag.manager.ports.service.casheService

import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag

@Service
class CacheService(cacheManager: CacheManager) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val cache = cacheManager.getCache("featureFlagsCache")

    fun addToCache(key: String, featureFlagList: FeatureFlagList) {
        cache?.putIfAbsent(key, featureFlagList)
        log.info("Adding to cache $featureFlagList")
    }

    fun getFromCache(key: String): FeatureFlagList {
        val featureFlagList = cache?.get(key)?.get() as FeatureFlagList
        log.info("Getting from cache $featureFlagList")
        return featureFlagList
    }

    fun contains(key: String): Boolean {
        val featureFlagList = cache?.get(key)?.get()
        val isContains = featureFlagList != null
        log.info("key $key in cache: $isContains")
        return isContains
    }

    fun removeFromCache(key: String) {
        if (contains(key)) {
            val featureFlagList = cache?.get(key)?.get() as FeatureFlagList
            cache.evict(key)
            log.info("Removing from cache $featureFlagList")
        }
    }

    fun getKey(file: MultipartFile): String {
        val key = DigestUtils.md5Hex(file.bytes)
        log.info("File $file has key $key")
        return key
    }
}

data class FeatureFlagList(val featureFlags: List<FeatureFlag>)