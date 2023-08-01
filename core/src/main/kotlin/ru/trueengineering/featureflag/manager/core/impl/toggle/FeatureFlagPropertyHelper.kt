package ru.trueengineering.featureflag.manager.core.impl.toggle

import org.springframework.stereotype.Component
import ru.trueengineering.featureflag.manager.core.domen.changes.Difference
import ru.trueengineering.featureflag.manager.core.domen.changes.FeatureChanges
import ru.trueengineering.featureflag.manager.core.domen.changes.FeatureCompareFields
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

@Component
class FeatureFlagPropertyHelper {

    fun getChanges(
        featureFlag: FeatureFlag,
        newFeatureFlag: FeatureFlag
    ): FeatureChanges {
        val featureChanges = FeatureChanges()

        getFeatureFlagProperties(featureFlag)
            .filter { it.name in FeatureCompareFields.getNames() }
            .forEach { compareAndPut(featureChanges, it.name, featureFlag, newFeatureFlag) }

        return featureChanges
    }

    private fun compareAndPut(
        featureChanges: FeatureChanges,
        propertyName: String,
        featureFlag: FeatureFlag,
        newFeatureFlag: FeatureFlag
    ) {
        val oldProperty = getPropertyValueByName(featureFlag, propertyName)
        val newProperty = getPropertyValueByName(newFeatureFlag, propertyName)

        if (oldProperty != newProperty) {
            if (newProperty is Map<*, *>) {
                oldProperty as Map<*, *>

                newProperty.keys.filter { newProperty[it] != oldProperty[it] }.forEach {
                    featureChanges.changes?.put(
                        FeatureCompareFields.valueOf(it.toString()),
                        Difference(oldProperty[it], newProperty[it])
                    )
                }
            } else {
                featureChanges.changes?.put(
                    FeatureCompareFields.findByName(propertyName),
                    Difference(oldProperty, newProperty)
                )
            }
        }
    }

    private fun getFeatureFlagProperties(featureFlag: FeatureFlag): Collection<KProperty1<FeatureFlag, *>> {
        return featureFlag.javaClass.kotlin.memberProperties
    }

    private fun getPropertyValueByName(featureFlag: FeatureFlag, propertyName: String): Any? {
        return getFeatureFlagProperties(featureFlag).first { it.name == propertyName }.getter.call(featureFlag)
    }

}