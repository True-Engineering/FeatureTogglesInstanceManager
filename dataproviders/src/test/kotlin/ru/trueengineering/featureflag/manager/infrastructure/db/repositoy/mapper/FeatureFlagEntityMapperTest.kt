package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper

import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import ru.trueengineering.featureflag.manager.core.domen.toggle.Environment
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlagType
import ru.trueengineering.featureflag.manager.core.domen.toggle.Strategy
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EnvironmentEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagEnvironmentStateEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagEnvironmentStatePK

class FeatureFlagEntityMapperTest(
    @Autowired override var uut: FeatureFlagEntityMapper
) : MapperSpec<FeatureFlagEntityMapper, FeatureFlag, FeatureFlagEntity> {

    override fun verifyEntity(actualEntity: FeatureFlagEntity) {

        assertThat(actualEntity)
            .extracting(
                FeatureFlagEntity::uid,
                FeatureFlagEntity::description,
                FeatureFlagEntity::group,
                FeatureFlagEntity::type,
                FeatureFlagEntity::tag,
                FeatureFlagEntity::sprint,
            )
            .containsExactly(
                "cool.feature.enabled",
                "description",
                "group",
                FeatureFlagType.SYSTEM,
                "tag",
                "sprint"
            )

        assertThat(actualEntity.environments)
            .extracting<Long> { it.primaryKey?.environment?.id }
            .containsExactlyInAnyOrder(1L, 2L)
        assertThat(actualEntity.environments)
            .extracting<String> { it.primaryKey?.environment?.name }
            .containsExactlyInAnyOrder("envName1", "envName2")

        assertThat(actualEntity.environments)
            .extracting<Boolean> { it.enable }
            .containsExactlyInAnyOrder(true, false)

        assertThat(actualEntity.environments.first { it.primaryKey?.environment?.id == 1L })
            .extracting(FeatureFlagEnvironmentStateEntity::strategy, FeatureFlagEnvironmentStateEntity::strategyParams)
            .containsExactly("strategy", "{\"param\":\"value\"}")
    }

    override fun buildDomain(): FeatureFlag {
        val environment2 = Environment(2L, "envName2", false)
        val environment1 = Environment(
            1L, "envName1", true,
            Strategy("strategy", mutableMapOf(Pair("param", "value")))
        )
        return FeatureFlag("cool.feature.enabled", mutableListOf(environment1, environment2)).apply {
            this.description = "description"
            this.group = "group"
            this.type = FeatureFlagType.SYSTEM
            this.tags = setOf("tag")
            this.sprint = "sprint"
        }
    }

    override fun verifyDomain(actual: FeatureFlag) {
        val expected = buildDomain()
        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected)

        assertThat(actual.environments[0].id).isEqualTo(1L)
        assertThat(actual.environments[1].id).isEqualTo(2L)
    }

    override fun buildEntity(): FeatureFlagEntity {
        val environmentEntity1 =
            EnvironmentEntity("envName1").apply {
                this.id = 1L
                this.authKeyHash = "AUTH_KEY_HASH1"
            }
        val environmentEntity2 =
            EnvironmentEntity("envName2").apply {
                this.id = 2L
                this.authKeyHash = "AUTH_KEY_HASH2"
            }
        val featureFlagEnvironmentStatePK1 = FeatureFlagEnvironmentStatePK(environmentEntity1)
        val featureFlagEnvironmentStatePK2 = FeatureFlagEnvironmentStatePK(environmentEntity2)
        val featureFlagEnvironmentStateEntity1 = FeatureFlagEnvironmentStateEntity(true).apply {
            this.primaryKey = featureFlagEnvironmentStatePK1
            this.strategy = "strategy"
            this.strategyParams = "{\"param\":\"value\"}"
        }
        val featureFlagEnvironmentStateEntity2 = FeatureFlagEnvironmentStateEntity(false).apply {
            this.primaryKey = featureFlagEnvironmentStatePK2
        }
        return FeatureFlagEntity("cool.feature.enabled").apply {
            this.description = "description"
            this.group = "group"
            this.type = FeatureFlagType.SYSTEM
            this.tag = "tag"
            this.sprint = "sprint"
            this.environments = mutableSetOf(featureFlagEnvironmentStateEntity2, featureFlagEnvironmentStateEntity1)
        }

    }
}