package ru.trueengineering.featureflag.manager.core.domen.toggle

import java.util.TreeMap

data class EnvironmentFeatureFlag(
    val uid: String,
    var enable: Boolean = false,
    var description: String? = null,
    var group: String? = null,
    var permissions: List<String> = ArrayList(),
    var customProperties: MutableMap<String, String> = TreeMap(),
    var flippingStrategy: EnvironmentStrategy? = null) {
    override fun toString(): String {
        return "FeatureFlag(uid=$uid, " +
                "enable=$enable, " +
                "description=$description, " +
                "group=$group, " +
                "permissions=$permissions, " +
                "customProperties=$customProperties, " +
                "flippingStrategy=$flippingStrategy)"
    }
}

data class EnvironmentStrategy(val className: String, val initParams: MutableMap<String, String> = TreeMap()) {

    override fun toString(): String {
        return "FeatureFlagStrategy(className=$className, initParams=$initParams)"
    }
}

data class EnvironmentFeatureFlagsWithUpdateStatus(val featureFlags: List<EnvironmentFeatureFlag>, val update: Boolean)
