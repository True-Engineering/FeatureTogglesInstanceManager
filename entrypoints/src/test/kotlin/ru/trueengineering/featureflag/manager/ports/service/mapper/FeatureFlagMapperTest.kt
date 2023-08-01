package ru.trueengineering.featureflag.manager.ports.service.mapper

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import ru.trueengineering.featureflag.manager.core.domen.toggle.Environment
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlagType
import ru.trueengineering.featureflag.manager.core.domen.toggle.Strategy
import ru.trueengineering.featureflag.manager.ports.rest.controller.FeatureFlagDto
import kotlin.test.assertEquals

class FeatureFlagMapperTest(@Autowired override var uut: FeatureFlagMapper) :
    MapperSpec<FeatureFlagMapper, FeatureFlag, FeatureFlagDto> {

    override fun verifyDto(actualDto: FeatureFlagDto) {
        assertEquals("ff.id", actualDto.uid)
        assertEquals("desc", actualDto.description)
        assertEquals("group", actualDto.group)
        assertEquals(FeatureFlagType.RELEASE, actualDto.type)
        assertEquals(setOf("WEB", "TEST"), actualDto.tags)
        assertEquals(1, actualDto.environments.size)
        assertEquals("uat-1", actualDto.environments[0].name)
        assertEquals(true, actualDto.environments[0].enable)
        val strategy = actualDto.environments[0].flippingStrategy
        if (strategy != null) {
            assertEquals("strategyType", strategy.type)
            assertEquals(mapOf("param" to "value"), strategy.initParams)
        }
        Assertions.assertEquals(actualDto.environments[0].permissions, mutableSetOf("READ", "EDIT"))

    }

    override fun buildDomain(): FeatureFlag {
        val featureFlag = FeatureFlag(
            "ff.id",
            mutableListOf(Environment(1, "uat-1", true, Strategy("strategyType", mutableMapOf("param" to "value"))))
        )
        featureFlag.description = "desc"
        featureFlag.group = "group"
        featureFlag.tags = setOf("WEB", "TEST")
        featureFlag.type = FeatureFlagType.RELEASE
        return featureFlag
    }

}