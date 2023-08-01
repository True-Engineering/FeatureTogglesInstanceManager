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

@Sql("/check_auth.sql")
@ContextConfiguration(classes = [AclMethodSecurityConfiguration::class, TestRoleDefiner::class])
class FeatureFlagDatabaseRepositorySecurityIT(@Autowired override var uut: FeatureFlagDatabaseRepository)
    : JpaRepositoryBaseTest<FeatureFlagDatabaseRepository>() {

    @Test
    @WithMockUser("user_1")
    internal fun `should fetch for user_1 feature flags only with allowed environments`() {
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
        var featureFlags = uut.getFeatureFlagsForProject(1)
        assertThat(featureFlags).hasSize(1)
        assertThat(featureFlags[0].environments.map { it.name }).containsExactlyInAnyOrder("prod", "uat")

        featureFlags = uut.getFeatureFlagsForProject(2)
        assertThat(featureFlags).hasSize(1)
        assertThat(featureFlags[0].environments.map { it.name }).containsExactlyInAnyOrder("prod", "uat", "qa")

        featureFlags = uut.getFeatureFlagsForProject(3)
        assertThat(featureFlags).hasSize(1)
        assertThat(featureFlags[0].environments).isEmpty()

        featureFlags = uut.getFeatureFlagsForProject(4)
        assertThat(featureFlags).hasSize(1)
        assertThat(featureFlags[0].environments.map { it.name }).containsExactlyInAnyOrder("dev")

        featureFlags = uut.getFeatureFlagsForProject(5)
        assertThat(featureFlags).hasSize(1)
        assertThat(featureFlags[0].environments).isEmpty()

        featureFlags = uut.getFeatureFlagsForProject(6)
        assertThat(featureFlags).isEmpty()
    }

    @Test
    @WithMockUser("user_1")
    internal fun `should fetch for user_1 feature flags only for allowed environment`() {
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
        var featureFlags = uut.getFeatureFlagsForEnvironment(9)
        assertThat(featureFlags).hasSize(1)
        assertThat(featureFlags[0].environments).isEmpty()

        featureFlags = uut.getFeatureFlagsForProject(2)
        assertThat(featureFlags).hasSize(1)
        assertThat(featureFlags[0].environments.map { it.name }).containsExactlyInAnyOrder("prod", "uat", "qa")
    }


    @Test
    @WithMockUser("user_2")
    internal fun `should fetch for user_2 feature flags only with allowed environments`() {
        /**
         * user_2 имеет следующие разрешения:
         * - READ_ORGANIZATION организации organization_3
         *  - EDIT для проекта organization_3:project_5
         * Таким образом для него доступна:
         * - организация organization_3 и проект project_5 со всеми окружениями, благодаря permission EDIT на уровне проекта
         */
        var featureFlags = uut.getFeatureFlagsForProject(1)
        assertThat(featureFlags).hasSize(1)
        assertThat(featureFlags[0].environments).isEmpty()

        featureFlags = uut.getFeatureFlagsForProject(2)
        assertThat(featureFlags).hasSize(1)
        assertThat(featureFlags[0].environments).isEmpty()

        featureFlags = uut.getFeatureFlagsForProject(3)
        assertThat(featureFlags).hasSize(1)
        assertThat(featureFlags[0].environments).isEmpty()

        featureFlags = uut.getFeatureFlagsForProject(4)
        assertThat(featureFlags).hasSize(1)
        assertThat(featureFlags[0].environments).isEmpty()

        featureFlags = uut.getFeatureFlagsForProject(5)
        assertThat(featureFlags).hasSize(1)
        assertThat(featureFlags[0].environments.map { it.name }).containsExactlyInAnyOrder("dev-1")

        featureFlags = uut.getFeatureFlagsForProject(6)
        assertThat(featureFlags).isEmpty()
    }

    @Test
    @WithMockUser("user_2")
    internal fun `should fetch for user_2 feature flags only for allowed environment`() {
        /**
         * user_2 имеет следующие разрешения:
         * - READ_ORGANIZATION организации organization_3
         *  - EDIT для проекта organization_3:project_5
         * Таким образом для него доступна:
         * - организация organization_3 и проект project_5 со всеми окружениями, благодаря permission EDIT на уровне проекта
         */
        var featureFlags = uut.getFeatureFlagsForEnvironment(9)
        assertThat(featureFlags).hasSize(1)
        assertThat(featureFlags[0].environments.map { it.name }).containsExactlyInAnyOrder("dev-1")

        featureFlags = uut.getFeatureFlagsForProject(2)
        assertThat(featureFlags).hasSize(1)
        assertThat(featureFlags[0].environments).isEmpty()
    }


}