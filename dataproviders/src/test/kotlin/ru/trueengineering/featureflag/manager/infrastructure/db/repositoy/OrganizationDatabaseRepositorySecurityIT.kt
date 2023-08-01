package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import ru.trueengineering.featureflag.manager.authorization.config.AclMethodSecurityConfiguration
import ru.trueengineering.featureflag.manager.authorization.config.impl.TestRoleDefiner
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.JpaRepositoryBaseTest

@ContextConfiguration(classes = [AclMethodSecurityConfiguration::class, TestRoleDefiner::class])
@Sql("/check_auth.sql")
class OrganizationDatabaseRepositorySecurityIT(
    @Autowired
    override var uut: OrganizationDatabaseRepository
) : JpaRepositoryBaseTest<OrganizationDatabaseRepository>() {

    @Test
    @WithMockUser("user_1")
    internal fun `should fetch for user_1 only allowed organizations, projects and env`() {
        /**
         * user_1 имеет следующие разрешения:
         * - READ_ORGANIZATION и EDIT для организации organization_1
         *  - READ_PROJECT для проекта organization_1:project_2
         * - READ_ORGANIZATION для организации organization_2
         *  - READ_PROJECT для проекта organization_2:project_4
         *      - READ_ENVIRONMENT для organization_2:project_4:dev
         * Таким образом для него доступна:
         * - организация organization_1 и все её проекты со всеми окружениями,
         * благодаря EDIT permission на уровне организации
         * - организация organization_2, её проект project_4 и его окружение dev
         */
        val actualOrganizations = uut.findAll()
        Assertions.assertThat(actualOrganizations).hasSize(2)
        val organizationsByName = actualOrganizations.groupBy { it.name }.toMap()
        Assertions.assertThat(organizationsByName).hasSize(2)
        Assertions.assertThat(organizationsByName).containsOnlyKeys("organization_1", "organization_2")

        Assertions.assertThat(organizationsByName["organization_1"]).hasSize(1)
        val firstOrganization = organizationsByName["organization_1"]!![0]
        Assertions.assertThat(firstOrganization.projects).hasSize(2)
        val firstOrganizationProjects = firstOrganization.projects.groupBy { it.name }.toMap()
        Assertions.assertThat(firstOrganizationProjects).containsOnlyKeys("project_1", "project_2")
        Assertions.assertThat(firstOrganizationProjects["project_1"]!![0].environments.map { it.name })
            .containsExactlyInAnyOrder("prod", "uat")
        Assertions.assertThat(firstOrganizationProjects["project_2"]!![0].environments.map { it.name })
            .containsExactlyInAnyOrder("prod", "uat", "qa")

        Assertions.assertThat(organizationsByName["organization_2"]).hasSize(1)
        val secondOrganization = organizationsByName["organization_2"]!![0]
        Assertions.assertThat(secondOrganization.projects).hasSize(1)
        val secondOrganizationProjects = secondOrganization.projects[0]
        Assertions.assertThat(secondOrganizationProjects.name).isEqualTo("project_4")
        Assertions.assertThat(secondOrganizationProjects.environments.map { it.name }).containsExactlyInAnyOrder("dev")
    }

    @Test
    @WithMockUser("user_2")
    internal fun `should fetch for user_2 only allowed organizations, projects and env`() {
        /**
         * user_2 имеет следующие разрешения:
         * - READ_ORGANIZATION организации organization_3
         *  - EDIT для проекта organization_3:project_5
         * Таким образом для него доступна:
         * - организация organization_3 и проект project_5 со всеми окружениями, благодаря permission EDIT на уровне проекта
         */
        val actualOrganizations = uut.findAll()
        Assertions.assertThat(actualOrganizations).hasSize(1)
        Assertions.assertThat(actualOrganizations[0].name).isEqualTo("organization_3")

        Assertions.assertThat(actualOrganizations[0].projects).hasSize(1)
        Assertions.assertThat(actualOrganizations[0].projects[0].name).isEqualTo("project_5")
        Assertions.assertThat(actualOrganizations[0].projects[0].environments).hasSize(1)
        Assertions.assertThat(actualOrganizations[0].projects[0].environments[0].name).isEqualTo("dev-1")
    }

}