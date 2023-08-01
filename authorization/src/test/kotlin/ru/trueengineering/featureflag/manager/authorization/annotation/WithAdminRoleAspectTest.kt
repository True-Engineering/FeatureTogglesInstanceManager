package ru.trueengineering.featureflag.manager.authorization.annotation

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.stereotype.Component
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.trueengineering.featureflag.manager.authorization.ADMIN_ROLE

@ExtendWith(SpringExtension::class)
@EnableGlobalMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true
)
@EnableAspectJAutoProxy
@ContextConfiguration(classes = [WithAdminRoleAspect::class, TestComponent::class, AuthorizedComponent::class])
internal class WithAdminRoleAspectTest {

    @Autowired
    private lateinit var testComponent: TestComponent

    @Autowired
    private lateinit var authorizedComponent: AuthorizedComponent;

    @Test
    @WithMockUser(username = "user_1", authorities = ["A", "B"])
    internal fun `should execute admin function with return value`() {
        assertThat(testComponent.adminMethodWithReturnValue("newValue_1")).isEqualTo("newValue_1")
        assertThat(authorizedComponent.state).isEqualTo("newValue_1")
        checkSecurityContextAfterEndOfAdminScope("user_1")
    }

    @Test
    @WithMockUser(username = "user_1", authorities = ["A", "B"])
    internal fun `should execute admin function without return value`() {
        testComponent.adminMethodWithoutReturnValue("newValue_2")
        assertThat(authorizedComponent.state).isEqualTo("newValue_2")
        checkSecurityContextAfterEndOfAdminScope("user_1")
    }

    @Test
    @WithMockUser(username = "user_1", authorities = ["A", "B", ADMIN_ROLE])
    internal fun `should execute admin function when admin permissions already exist`() {
        testComponent.adminMethodWithoutReturnValue("newValue_3")
        assertThat(authorizedComponent.state).isEqualTo("newValue_3")
        checkSecurityContextAfterEndOfAdminScope("user_1", adminPermissionExist = true)
    }

    @Test
    @WithMockUser(username = "user_1", authorities = ["A", "B"])
    internal fun `should execute admin function with aspect annotation and @PreAuthorize`() {
        testComponent.adminMethod("newValue_4")
        assertThat(authorizedComponent.state).isEqualTo("newValue_4")
        checkSecurityContextAfterEndOfAdminScope("user_1")
    }

    @Test
    @WithMockUser(username = "user_1", authorities = ["A", "B"])
    internal fun `should execute chain of methods with @WithAdminRole annotation`() {
        testComponent.withAdminRoleAnnotation("newValue_5")
        assertThat(authorizedComponent.state).isEqualTo("newValue_5")
        checkSecurityContextAfterEndOfAdminScope("user_1")
    }

    @Test
    @WithMockUser(username = "user_1", authorities = ["A", "B"])
    internal fun `should execute method witch throw exception`() {
        Assertions.assertThatThrownBy { testComponent.methodThrowException("newValue_6") }
            .isInstanceOf(java.lang.IllegalArgumentException::class.java)
            .hasMessage("newValue_6")
        assertThat(authorizedComponent.state).isEqualTo("newValue_6")
        checkSecurityContextAfterEndOfAdminScope("user_1")
    }


    @Test
    @WithMockUser(username = "user_1", authorities = ["A", "B"])
    internal fun `should throw error because of no permissions`() {
        Assertions.assertThatThrownBy { testComponent.adminMethodWithoutAnnotation("value") }
            .isInstanceOf(org.springframework.security.access.AccessDeniedException::class.java)
    }

    @Test
    @WithMockUser(username = "user_1", authorities = ["A", "B"])
    internal fun `should execute function with admin scope`() {
        AspectUtils.executeWithAdminRole {
            assertThat(authorizedComponent.securedMethodWithReturnValue("value_")).isEqualTo("value_")
            checkSecurityContextAfterEndOfAdminScope("user_1", adminPermissionExist = true)
        }
        assertThat(authorizedComponent.state).isEqualTo("value_")
        checkSecurityContextAfterEndOfAdminScope("user_1")
    }

    @Test
    internal fun `should execute authorized method when security context is empty`() {
        authorizedComponent.withAdminRoleAnnotation("value_12")
        assertThat(authorizedComponent.state).isEqualTo("value_12")
        assertThat(SecurityContextHolder.getContext()?.authentication).isNull()
    }

    @Test
    internal fun `should execute authorized method with throwing exception when security context is empty`() {
        Assertions.assertThatThrownBy { testComponent.methodThrowException("value_123") }
            .isInstanceOf(java.lang.IllegalArgumentException::class.java)
            .hasMessage("value_123")
        assertThat(authorizedComponent.state).isEqualTo("value_123")
        assertThat(SecurityContextHolder.getContext()?.authentication).isNull()
    }


    private fun checkSecurityContextAfterEndOfAdminScope(userName: String, adminPermissionExist: Boolean = false) {
        val authentication = SecurityContextHolder.getContext().authentication
        assertThat(
            authentication
                ?.authorities
                ?.map { it.authority }
                ?.any { it.equals(ADMIN_ROLE) }
        ).isEqualTo(adminPermissionExist)

        assertThat(
            authentication
                ?.authorities
                ?.map { it.authority }
        ).contains("A", "B")

        val principal = authentication.principal
        principal as User
        assertThat(principal.username).isEqualTo(userName)
    }

}

@Component
class TestComponent {

    @Autowired
    private lateinit var authorizedComponent: AuthorizedComponent;

    @WithAdminRole
    fun adminMethodWithReturnValue(newValue: String): String {
        return authorizedComponent.securedMethodWithReturnValue(newValue)
    }

    @WithAdminRole
    fun adminMethodWithoutReturnValue(newValue: String) {
        authorizedComponent.securedMethodWithoutReturnValue(newValue)
    }

    @WithAdminRole
    @PreAuthorize("hasRole('FEATURE_FLAGS_ADMIN')")
    fun adminMethod(newValue: String) {
        authorizedComponent.unsecure(newValue)
    }

    @WithAdminRole
    @PreAuthorize("hasRole('FEATURE_FLAGS_ADMIN')")
    fun withAdminRoleAnnotation(newValue: String) {
        authorizedComponent.withAdminRoleAnnotation(newValue)
    }

    @WithAdminRole
    fun methodThrowException(newValue: String) {
        authorizedComponent.securedMethodWithoutReturnValue(newValue)
        throw java.lang.IllegalArgumentException(newValue)
    }


    @PreAuthorize("hasRole('FEATURE_FLAGS_ADMIN')")
    fun adminMethodWithoutAnnotation(newValue: String) {

    }

}

@Component
class AuthorizedComponent {

    var state: String = ""

    @PreAuthorize("hasRole('FEATURE_FLAGS_ADMIN')")
    fun securedMethodWithReturnValue(newValue: String): String {
        state = newValue
        return state
    }

    @PreAuthorize("hasRole('FEATURE_FLAGS_ADMIN')")
    fun securedMethodWithoutReturnValue(newValue: String) {
        state = newValue
    }

    fun unsecure(newValue: String) {
        state = newValue
    }

    @WithAdminRole
    @PreAuthorize("hasRole('FEATURE_FLAGS_ADMIN')")
    fun withAdminRoleAnnotation(newValue: String) {
        state = newValue
    }

}