package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper

import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.environment.Instance
import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.organization.Organization
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EnvironmentEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.InstanceEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.OrganizationEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ProjectEntity
import java.time.Instant
import kotlin.test.assertEquals

class OrganizationEntityMapperTest(
    @Autowired override var uut: OrganizationEntityMapper
) : MapperSpec<OrganizationEntityMapper, Organization, OrganizationEntity> {

    private val instant: Instant = Instant.now()

    override fun verifyEntity(actualEntity: OrganizationEntity) {

        assertEquals(1L, actualEntity.id)
        assertEquals("orgName", actualEntity.name)
        assertThat(actualEntity.projects.first())
            .extracting(ProjectEntity::id, ProjectEntity::name)
            .containsExactly(2L, "prj")
        assertThat(actualEntity.projects.first().environments.first())
            .extracting(EnvironmentEntity::id, EnvironmentEntity::name, EnvironmentEntity::authKeyHash)
            .containsExactly(2L, "envName", "AUTH_KEY_HASH")
        assertThat(actualEntity.projects.first().environments.first().instances.first())
            .extracting(InstanceEntity::id, InstanceEntity::name, InstanceEntity::status)
            .containsExactly(1L, "instance", InstanceConnectionStatus.OUT_OF_SYNC)

    }

    override fun buildDomain(): Organization {
        val instance = Instance(1L, "instance", instant, InstanceConnectionStatus.OUT_OF_SYNC)
        val environment = Environment(2L, "envName", "AUTH_KEY_HASH", instances = listOf(instance))
        val project = Project(2L, "prj", mutableListOf(environment))
        return Organization(1L, "orgName", listOf(project))
    }

    override fun verifyDomain(actual: Organization) {
        val expected = buildDomain()
        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected)

    }

    override fun buildEntity(): OrganizationEntity {
        val instanceEntity = InstanceEntity("instance").apply {
            id = 1
            status = InstanceConnectionStatus.OUT_OF_SYNC
            updated = instant

        }
        val environmentEntity =
            EnvironmentEntity("envName").apply {
                this.id = 2L
                this.instances = mutableSetOf(instanceEntity)
                this.authKeyHash = "AUTH_KEY_HASH"
            }
        val projectEntity = ProjectEntity("prj").apply {
            this.id = 2L
            this.environments = mutableSetOf(environmentEntity)
        }

        return OrganizationEntity("orgName").apply {
            id = 1L
            projects = mutableSetOf(projectEntity)
        }
    }
}