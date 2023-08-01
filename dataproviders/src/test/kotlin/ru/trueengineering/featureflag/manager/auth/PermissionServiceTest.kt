package ru.trueengineering.featureflag.manager.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.acls.model.Permission
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import ru.trueengineering.featureflag.manager.authorization.ADMIN_ROLE
import ru.trueengineering.featureflag.manager.authorization.config.AuthorizationConfiguration
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.CREATE_PROJECT
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.DELETE
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.EDIT
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.READ_ORGANIZATION
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.READ_PROJECT
import ru.trueengineering.featureflag.manager.authorization.impl.BusinessEntityImpl
import ru.trueengineering.featureflag.manager.authorization.impl.PermissionFilter
import ru.trueengineering.featureflag.manager.authorization.config.impl.TestRoleDefiner
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.JpaRepositoryBaseTest
import java.security.Principal

@ContextConfiguration(
    classes = [AuthorizationConfiguration::class, TestPermissionFilter::class, TestRoleDefiner::class]
)
@Sql(
    "/organization_project_dataset.sql", "/authorization.sql", "/update_id_sequences.sql",
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
internal class PermissionServiceTest(@Autowired override var uut: IPermissionService) :
    JpaRepositoryBaseTest<IPermissionService>() {

    private val aclClass: String = "ru.trueengineering.featureflag.manager.core.domen.organization.Organization"
    private val user1: Principal = Principal { "user_1" }
    private val user2: Principal = Principal { "user_2" }

    @Test
    @WithMockUser(username = "user_1")
    internal fun `should fetch permissions for user`() {
        val businessEntity = BusinessEntityImpl(1L, aclClass)
        val actualPermissions = uut.getPermissionsForCurrentUser(businessEntity)
        assertThat(actualPermissions)
            .containsExactlyInAnyOrder(READ_PROJECT, READ_ORGANIZATION)
    }

    @Test
    @WithMockUser(username = "user_1", authorities = [ADMIN_ROLE, "ROLE_READER"])
    internal fun `should fetch permissions for current user with role`() {
        val businessEntity = BusinessEntityImpl(1L, aclClass)
        val actualPermissions = uut.getPermissionsForCurrentUser(businessEntity)
        assertThat(actualPermissions)
            .containsExactlyInAnyOrder(READ_PROJECT, READ_ORGANIZATION, CREATE_PROJECT, EDIT)
    }


    @Test
    @WithMockUser(username = "user_1")
    internal fun `should fetch permissions for another user`() {
        val businessEntity = BusinessEntityImpl(1L, aclClass)
        val actualPermissions = uut.getPermissionsForUser(listOf(businessEntity), user2)

        assertThat(actualPermissions).isNotNull
        assertThat(actualPermissions.userName).isEqualTo("user_2")
        assertThat(actualPermissions.permissions).hasSize(1)
        assertThat(actualPermissions.permissions[0].entity.type).isEqualTo(businessEntity.type)
        assertThat(actualPermissions.permissions[0].entity.identifier).isEqualTo(businessEntity.identifier)
        assertThat(actualPermissions.permissions[0].permissions)
            .containsExactlyInAnyOrder(READ_PROJECT)
    }

    @Test
    @WithMockUser(username = "user_2")
    internal fun `should fetch permissions for users`() {
        val organization = BusinessEntityImpl(1L, aclClass)
        val environment = BusinessEntityImpl(1L, "ru.trueengineering.featureflag.manager.core.domen.environment.Environment")
        val actualPermissions = uut.getPermissionsForUsers(listOf(organization, environment), listOf(user1, user2))

        assertThat(actualPermissions).isNotNull
        assertThat(actualPermissions).hasSize(2)
        val user_1Permission = actualPermissions[0]
        assertThat(user_1Permission.userName).isEqualTo("user_1")
        assertThat(user_1Permission.permissions).hasSize(2)
        val organizationUser_1 = user_1Permission.findByObjectIdentity(organization)!!
        assertThat(organizationUser_1.entity.type).isEqualTo(organization.type)
        assertThat(organizationUser_1.entity.identifier).isEqualTo(organization.identifier)
        assertThat(organizationUser_1.permissions)
            .containsExactlyInAnyOrder(READ_PROJECT, READ_ORGANIZATION)
        val environmentUser_1 = user_1Permission.findByObjectIdentity(environment)!!
        assertThat(environmentUser_1.entity.type).isEqualTo(environment.type)
        assertThat(environmentUser_1.entity.identifier).isEqualTo(environment.identifier)
        assertThat(environmentUser_1.permissions)
            .containsExactlyInAnyOrder(CustomPermission.READ_ENVIRONMENT)


        val user_2Permission = actualPermissions[1]
        assertThat(user_2Permission.userName).isEqualTo("user_2")
        assertThat(user_2Permission.permissions).hasSize(2)
        val organizationUser_2 = user_2Permission.findByObjectIdentity(organization)!!
        assertThat(organizationUser_2.entity.type).isEqualTo(organization.type)
        assertThat(organizationUser_2.entity.identifier).isEqualTo(organization.identifier)
        assertThat(organizationUser_2.permissions)
            .containsExactlyInAnyOrder(READ_PROJECT)
        val environmentUser_2 = user_2Permission.findByObjectIdentity(environment)!!
        assertThat(environmentUser_2.entity.type).isEqualTo(environment.type)
        assertThat(environmentUser_2.entity.identifier).isEqualTo(environment.identifier)
        assertThat(environmentUser_2.permissions).isEmpty()
    }

    @Test
    @WithMockUser(username = "user_1", authorities = [ADMIN_ROLE])
    internal fun `should grant new permission to user`() {
        val businessEntity = BusinessEntityImpl(1L, aclClass)
        uut.grantPermissionsForUser(businessEntity, listOf(CustomPermission.EDIT, DELETE), user2)

        val actualPermissions = uut.getPermissionsForUser(listOf(businessEntity), user2)

        assertThat(actualPermissions).isNotNull
        assertThat(actualPermissions.userName).isEqualTo("user_2")
        assertThat(actualPermissions.permissions).hasSize(1)
        assertThat(actualPermissions.permissions[0].entity.type).isEqualTo(businessEntity.type)
        assertThat(actualPermissions.permissions[0].entity.identifier).isEqualTo(businessEntity.identifier)
        assertThat(actualPermissions.permissions[0].permissions)
            .containsExactlyInAnyOrder(READ_PROJECT, CustomPermission.EDIT, DELETE)
    }

    @Test
    @WithMockUser(username = "user_1", authorities = [ADMIN_ROLE])
    internal fun `should grant idempotent new permission to user`() {
        val businessEntity = BusinessEntityImpl(1L, aclClass)
        // дважды добавляем разрешение, не должно произойти ошибок и результат должен быть таким же
        // как и после первого добавления
        uut.grantPermissionsForUser(businessEntity, listOf(CustomPermission.EDIT), user2)
        uut.grantPermissionsForUser(businessEntity, listOf(CustomPermission.EDIT), user2)

        val actualPermissions = uut.getPermissionsForUser(listOf(businessEntity), user2)

        assertThat(actualPermissions).isNotNull
        assertThat(actualPermissions.userName).isEqualTo("user_2")
        assertThat(actualPermissions.permissions).hasSize(1)
        assertThat(actualPermissions.permissions[0].entity.type).isEqualTo(businessEntity.type)
        assertThat(actualPermissions.permissions[0].entity.identifier).isEqualTo(businessEntity.identifier)
        assertThat(actualPermissions.permissions[0].permissions)
            .containsExactlyInAnyOrder(READ_PROJECT, CustomPermission.EDIT)
    }

    @Test
    @WithMockUser(username = "user_1", authorities = [ADMIN_ROLE])
    internal fun `should clear idempotent permission to user`() {
        val businessEntity = BusinessEntityImpl(1L, aclClass)

        // дважды удаляем разрешение, не должно произойти ошибок и результат должен быть таким же
        // как и после первого удаления
        uut.clearPermissionsForUser(businessEntity,user2)
        uut.clearPermissionsForUser(businessEntity,user2)

        val actualPermissions = uut.getPermissionsForUser(listOf(businessEntity), user2)

        assertThat(actualPermissions).isNotNull
        assertThat(actualPermissions.userName).isEqualTo("user_2")
        assertThat(actualPermissions.permissions).hasSize(1)
        assertThat(actualPermissions.permissions[0].entity.type).isEqualTo(businessEntity.type)
        assertThat(actualPermissions.permissions[0].entity.identifier).isEqualTo(businessEntity.identifier)
        assertThat(actualPermissions.permissions[0].permissions).isEmpty()
    }

    @Test
    @WithMockUser(username = "user_1", authorities = [ADMIN_ROLE])
    internal fun `should clear permission to user`() {
        val businessEntity = BusinessEntityImpl(1L, aclClass)
        uut.clearPermissionsForUser(businessEntity, user2)

        val actualPermissions = uut.getPermissionsForUser(listOf(businessEntity), user2)

        assertThat(actualPermissions).isNotNull
        assertThat(actualPermissions.userName).isEqualTo("user_2")
        assertThat(actualPermissions.permissions).hasSize(1)
        assertThat(actualPermissions.permissions[0].entity.type).isEqualTo(businessEntity.type)
        assertThat(actualPermissions.permissions[0].entity.identifier).isEqualTo(businessEntity.identifier)
        assertThat(actualPermissions.permissions[0].permissions).isEmpty()
    }

    @Test
    @WithMockUser(username = "user_1", authorities = [ADMIN_ROLE])
    internal fun `should clear multiple permission to user`() {
        val businessEntity = BusinessEntityImpl(1L, aclClass)
        uut.clearPermissionsForUser(businessEntity, user1)

        val actualPermissions = uut.getPermissionsForUser(listOf(businessEntity), user1)

        assertThat(actualPermissions).isNotNull
        assertThat(actualPermissions.userName).isEqualTo("user_1")
        assertThat(actualPermissions.permissions).hasSize(1)
        assertThat(actualPermissions.permissions[0].entity.type).isEqualTo(businessEntity.type)
        assertThat(actualPermissions.permissions[0].entity.identifier).isEqualTo(businessEntity.identifier)
        assertThat(actualPermissions.permissions[0].permissions).isEmpty()
    }

    @Test
    @WithMockUser(username = "user_1")
    internal fun `should check permission exist for user`() {
        val businessEntity = BusinessEntityImpl(1L, aclClass)

        assertThat(uut.isGrantedPermission(businessEntity, READ_PROJECT, user2)).isTrue
        assertThat(uut.isGrantedPermission(businessEntity, CustomPermission.EDIT, user2)).isFalse

        assertThat(uut.isGrantedPermission(businessEntity, READ_PROJECT, user1)).isTrue
        assertThat(uut.isGrantedPermission(businessEntity, READ_ORGANIZATION, user1)).isTrue
        assertThat(uut.isGrantedPermission(businessEntity, DELETE, user1)).isFalse
    }

    @Test
    @WithMockUser(username = "user_1")
    internal fun `should not grant empty list of permissions`() {
        val businessEntity = BusinessEntityImpl(1L, aclClass)

        val before = uut.getPermissionsForUser(listOf(businessEntity), user2)
        uut.grantPermissionsForUser(businessEntity, listOf(), user2)
        val after = uut.getPermissionsForUser(listOf(businessEntity), user2)
        assertThat(before).isEqualTo(after)
    }

    @Test
    @WithMockUser(username = "user_1", authorities = [ADMIN_ROLE])
    fun getUsersByEntity() {
        val type = "ru.trueengineering.featureflag.manager.core.domen.project.Project"
        val businessEntity = BusinessEntityImpl(1L, type)
        val usersByEntity =
            uut.getUsersByEntity(businessEntity, listOf(READ_PROJECT, CustomPermission.EDIT))
        assertThat(usersByEntity)
            .isNotEmpty
            .containsExactlyInAnyOrder("user_1", "user_2", "user_3")
    }

    @Test
    internal fun `should filter environment permissions`() {
        val environment =
            BusinessEntityImpl(1L, "ru.trueengineering.featureflag.manager.core.domen.environment.Environment")
        val permissionsForUser = uut.getPermissionsForUser(environment, user1)
        assertThat(permissionsForUser).hasSize(1)
        assertThat(permissionsForUser).containsExactlyInAnyOrder(CustomPermission.READ_ENVIRONMENT)
    }

    @Test
    @WithMockUser(username = "user_1")
    internal fun `should fetch permissions names for current principal user`() {
        val businessEntity = BusinessEntityImpl(1L, aclClass)
        val actualPermissions = uut.getPermissionsNameListForCurrentUser(businessEntity)

        assertThat(actualPermissions).isNotNull.hasSize(2)
        assertThat(actualPermissions).containsExactlyInAnyOrder("READ_PROJECT", "READ_ORGANIZATION")
    }

    @Test
    @WithMockUser(username = "user_1", authorities = [ADMIN_ROLE])
    internal fun `should fetch permissions names for current principal user and role`() {
        val businessEntity = BusinessEntityImpl(1L, aclClass)
        val actualPermissions = uut.getPermissionsNameListForCurrentUser(businessEntity)

        assertThat(actualPermissions).isNotNull.hasSize(4)
        assertThat(actualPermissions).containsExactlyInAnyOrder("READ_PROJECT", "READ_ORGANIZATION", "EDIT", "CREATE_PROJECT")
    }
}

class TestPermissionFilter : PermissionFilter {
    override fun isApplicable(entity: BusinessEntity): Boolean =
        entity.type == "ru.trueengineering.featureflag.manager.core.domen.environment.Environment"

    override fun isAllowed(permission: Permission): Boolean {
        return permission.mask == CustomPermission.READ_ENVIRONMENT.mask
    }
}