package ru.trueengineering.featureflag.manager.ports.service.casheService

import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import java.util.concurrent.TimeUnit
import org.springframework.cache.CacheManager
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.Collections

@EnableCaching
@Configuration
class CaffeineCacheConfig {
    @Bean
    fun caffeineCacheManager(): CacheManager? {
        val simpleCacheManager = SimpleCacheManager()

        val featureFlagsCache = CaffeineCache(
            "featureFlagsCache", Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .maximumSize(1000).build()
        )
        simpleCacheManager.setCaches(Collections.singletonList(featureFlagsCache))
        return simpleCacheManager
    }
}