package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper

import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.environment.Instance
import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EnvironmentEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.InstanceEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ProjectEntity
import java.time.Instant

class ProjectEntityMapperTest(
    @Autowired override var uut: ProjectEntityMapper
) : MapperSpec<ProjectEntityMapper, Project, ProjectEntity> {

    private val instant: Instant = Instant.now()

    override fun verifyEntity(actualEntity: ProjectEntity) {

        assertThat(actualEntity)
            .extracting(ProjectEntity::id, ProjectEntity::name)
            .containsExactly(2L, "prj")

        assertThat(actualEntity.environments)
            .extracting<Long> { it.id }
            .containsExactlyInAnyOrder(1L, 2L)
        assertThat(actualEntity.environments)
            .extracting<String> { it.name }
            .containsExactlyInAnyOrder("envName1", "envName2")
        assertThat(actualEntity.environments)
            .extracting<String> { it.authKeyHash }
            .containsExactlyInAnyOrder("AUTH_KEY_HASH1", "AUTH_KEY_HASH2")
        assertThat(actualEntity.environments.flatMap { it.instances }.first())
            .extracting(InstanceEntity::id, InstanceEntity::name, InstanceEntity::status)
            .containsExactly(1L, "instance", InstanceConnectionStatus.OUT_OF_SYNC)

    }

    override fun buildDomain(): Project {
        val instance = Instance(1L, "instance", instant, InstanceConnectionStatus.OUT_OF_SYNC)
        val environment2 = Environment(2L, "envName2", "AUTH_KEY_HASH2", instances = listOf(instance))
        val environment1 = Environment(1L, "envName1", "AUTH_KEY_HASH1", instances = listOf())
        val project = Project(2, "prj", mutableListOf(environment1, environment2))
        return project
    }

    override fun verifyDomain(actual: Project) {
        val expected = buildDomain()
        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected)

        assertThat(actual.environments[0].id).isEqualTo(1L)
        assertThat(actual.environments[1].id).isEqualTo(2L)
    }

    override fun buildEntity(): ProjectEntity {
        val instanceEntity = InstanceEntity("instance").apply {
            id = 1L
            status = InstanceConnectionStatus.OUT_OF_SYNC
            updated = instant

        }
        val environmentEntity1 = EnvironmentEntity("envName1").apply {
            this.id = 1L
            this.instances = mutableSetOf()
            this.authKeyHash = "AUTH_KEY_HASH1"
        }
        val environmentEntity2 =
            EnvironmentEntity("envName2").apply {
                this.id = 2L
                this.instances = mutableSetOf(instanceEntity)
                this.authKeyHash = "AUTH_KEY_HASH2"
            }
        val projectEntity = ProjectEntity("prj").apply {
            this.id = 2L
            this.environments = mutableSetOf(environmentEntity2, environmentEntity1)
        }

        return projectEntity
    }
}