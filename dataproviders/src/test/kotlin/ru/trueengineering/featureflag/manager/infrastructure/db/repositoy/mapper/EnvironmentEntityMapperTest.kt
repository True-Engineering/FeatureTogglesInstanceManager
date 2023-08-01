package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper

import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.environment.Instance
import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EnvironmentEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.InstanceEntity
import java.time.Instant

class EnvironmentEntityMapperTest(
    @Autowired override var uut: EnvironmentEntityMapper
) : MapperSpec<EnvironmentEntityMapper, Environment, EnvironmentEntity> {

    private val instant: Instant = Instant.now()

    override fun verifyEntity(actualEntity: EnvironmentEntity) {

        assertThat(actualEntity)
            .extracting(EnvironmentEntity::id, EnvironmentEntity::name, EnvironmentEntity::authKeyHash)
            .containsExactly(2L, "envName", "AUTH_KEY_HASH")
        assertThat(actualEntity.instances.first())
            .extracting(InstanceEntity::id, InstanceEntity::name, InstanceEntity::status)
            .containsExactly(1L, "instance", InstanceConnectionStatus.OUT_OF_SYNC)

    }

    override fun buildDomain(): Environment {
        val instance = Instance(1L, "instance", instant, InstanceConnectionStatus.OUT_OF_SYNC)
        val environment = Environment(2L, "envName", "AUTH_KEY_HASH", instances = listOf(instance))
        return environment
    }

    override fun verifyDomain(actual: Environment) {
        val expected = buildDomain()
        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected)

    }

    override fun buildEntity(): EnvironmentEntity {
        val instanceEntity = InstanceEntity("instance").apply {
            id = 1L
            status = InstanceConnectionStatus.OUT_OF_SYNC
            updated = instant

        }
        val environmentEntity =
            EnvironmentEntity("envName").apply {
                this.id = 2L
                this.instances = mutableSetOf(instanceEntity)
                this.authKeyHash = "AUTH_KEY_HASH"
            }

        return environmentEntity
    }
}