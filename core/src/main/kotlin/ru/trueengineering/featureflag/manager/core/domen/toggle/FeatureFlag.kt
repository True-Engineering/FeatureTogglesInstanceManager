package ru.trueengineering.featureflag.manager.core.domen.toggle

import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import java.util.EnumMap

typealias FeatureFlagProperties = MutableMap<FeatureFlagPropertiesClass, String>

data class FeatureFlag(
    val uid: String, 
    var environments: MutableList<Environment> = ArrayList(),
    var id: Long? = null,
    var description: String? = "",
    var group: String? = "",
    var type: FeatureFlagType? = null,
    var tags: Set<String> = HashSet(),
    var sprint: String? = "",
    var properties: FeatureFlagProperties = EnumMap(FeatureFlagPropertiesClass::class.java)
)

data class Strategy(val type: String, val initParams: MutableMap<String, String> = HashMap())

data class Environment(val id: Long, val name: String, var enable: Boolean = false, var strategy: Strategy? = null) :
    BusinessEntity {
    override fun getBusinessId() = id

    override fun getType(): String =
        ru.trueengineering.featureflag.manager.core.domen.environment.Environment::class.java.name
}

enum class FeatureFlagType {
    RELEASE,
    SYSTEM
}

enum class FeatureFlagPropertiesClass {
}
