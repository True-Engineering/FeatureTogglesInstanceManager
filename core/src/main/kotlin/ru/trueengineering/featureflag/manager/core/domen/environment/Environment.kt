package ru.trueengineering.featureflag.manager.core.domen.environment

import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import java.io.Serializable
import java.util.EnumMap

typealias EnvironmentProperties = MutableMap<EnvironmentPropertiesClass, String?>

data class Environment(
    var id: Long? = null,
    var name: String,
    var authKeyHash: String? = null,
    val instances: List<Instance> = listOf(),
    var emails: List<Email> = listOf(),
    var properties: EnvironmentProperties = EnumMap(EnvironmentPropertiesClass::class.java)
) : BusinessEntity {
    override fun getBusinessId(): Serializable? = id

    override fun getType(): String = Environment::class.java.name
}

enum class EnvironmentPropertiesClass {
    FREEZING_ENABLE,
    FREEZING_USER,
    FREEZING_END_TIME
}