package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import org.assertj.core.api.Assertions.assertThat
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
class EnvironmentDatabaseRepositorySecurityIT(
    @Autowired
    override var uut: EnvironmentDatabaseRepository
) : JpaRepositoryBaseTest<EnvironmentDatabaseRepository>() {

    @Test
    @WithMockUser("user_1")
    internal fun `should fetch for user_1 only allowed environment`() {
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
        // organization_1:project_1
        assertThat(uut.getByProjectId(1).map { it.name }).containsExactlyInAnyOrder("prod", "uat")
        // organization_1:project_2
        assertThat(uut.getByProjectId(2).map { it.name }).containsExactlyInAnyOrder("prod", "uat", "qa")

        // organization_2:project_3
        assertThat(uut.getByProjectId(3)).isEmpty()
        // organization_2:project_4
        assertThat(uut.getByProjectId(4).map { it.name }).containsExactlyInAnyOrder("dev")

        // organization_3:project_5
        assertThat(uut.getByProjectId(5)).isEmpty()
        // organization_3:project_6
        assertThat(uut.getByProjectId(6)).isEmpty()
    }

    @Test
    @WithMockUser("user_2")
    internal fun `should fetch for user_2 only allowed environment`() {
        /**
         * user_2 имеет следующие разрешения:
         * - READ_ORGANIZATION организации organization_3
         *  - EDIT для проекта organization_3:project_5
         * Таким образом для него доступна:
         * - организация organization_3 и проект project_5 со всеми окружениями, благодаря permission EDIT на уровне проекта
         */
        // organization_1:project_1
        assertThat(uut.getByProjectId(1)).isEmpty()
        // organization_1:project_2
        assertThat(uut.getByProjectId(2)).isEmpty()

        // organization_2:project_3
        assertThat(uut.getByProjectId(3)).isEmpty()
        // organization_2:project_4
        assertThat(uut.getByProjectId(4)).isEmpty()

        // organization_3:project_5
        assertThat(uut.getByProjectId(5).map { it.name }).containsExactlyInAnyOrder("dev-1")
        // organization_3:project_5
        assertThat(uut.getByProjectId(6)).isEmpty()
    }

}