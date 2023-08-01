package ru.trueengineering.featureflag.manager.ports.rest.mdc.aspect

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.bind.annotation.RestController
import ru.trueengineering.featureflag.manager.ports.rest.EnvironmentId
import ru.trueengineering.featureflag.manager.ports.rest.FeatureFlagId
import ru.trueengineering.featureflag.manager.ports.rest.MdcAspect
import ru.trueengineering.featureflag.manager.ports.rest.OrganizationId
import ru.trueengineering.featureflag.manager.ports.rest.ProjectId
import ru.trueengineering.featureflag.manager.ports.rest.controller.EnvironmentInfoDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.OrganizationDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.ProjectDto
import ru.trueengineering.featureflag.manager.ports.service.EnvironmentService
import ru.trueengineering.featureflag.manager.ports.service.OrganizationService
import ru.trueengineering.featureflag.manager.ports.service.ProjectService

private const val ORGANIZATION_ID = 100L
private const val PROJECT_ID = 11L
private const val ENVIRONMENT_ID = 12L
private const val FEATURE_FLAG = "cool.feature"

@ExtendWith(SpringExtension::class)
@EnableAspectJAutoProxy
@ContextConfiguration(classes = [MdcAspect::class, TestComponent::class])
internal class MdcAspectTest {

    @Autowired
    private lateinit var testComponent: TestComponent

    @MockBean
    private lateinit var organizationService: OrganizationService
    @MockBean
    private lateinit var projectService: ProjectService
    @MockBean
    private lateinit var environmentService: EnvironmentService

    @BeforeEach
    internal fun setUp() {
        Mockito.`when`(organizationService.searchById(ORGANIZATION_ID))
                .thenReturn(OrganizationDto(ORGANIZATION_ID, "orgName"))
        Mockito.`when`(projectService.searchById(PROJECT_ID))
                .thenReturn(ProjectDto(PROJECT_ID, "project"))
        Mockito.`when`(environmentService.searchById(ENVIRONMENT_ID))
                .thenReturn(EnvironmentInfoDto(ENVIRONMENT_ID, "env", listOf(), true, emails = listOf()))

    }

    @AfterEach
    internal fun tearDown() {
        assertThat(MDC.get("organizationName")).isBlank
        assertThat(MDC.get("projectName")).isBlank
        assertThat(MDC.get("environmentName")).isBlank
        assertThat(MDC.get("featureFlagUUID")).isBlank
    }

    @Test
    internal fun `should set organizationName MDC property`() {
        testComponent.withOrganizationId(ORGANIZATION_ID)
        //check that method was executed
        assertThat(testComponent.value).isEqualTo("1")
        Mockito.verify(organizationService).searchById(ORGANIZATION_ID)
    }

    @Test
    internal fun `should set projectName MDC property`() {
        testComponent.withProjectId(PROJECT_ID)
        //check that method was executed
        assertThat(testComponent.value).isEqualTo("2")
        Mockito.verify(projectService).searchById(PROJECT_ID)
    }

    @Test
    internal fun `should set environment MDC property`() {
        testComponent.withEnvironmentId(ENVIRONMENT_ID)
        //check that method was executed
        assertThat(testComponent.value).isEqualTo("3")
        Mockito.verify(environmentService).searchById(ENVIRONMENT_ID)
    }

    @Test
    internal fun `should set organizationName and projectName MDC property`() {
        testComponent.withOrganizationIdAndProjectId(ORGANIZATION_ID, PROJECT_ID)
        assertThat(testComponent.value).isEqualTo("4")
        Mockito.verify(organizationService).searchById(ORGANIZATION_ID)
        Mockito.verify(projectService).searchById(PROJECT_ID)
    }

    @Test
    internal fun `should set organizationName and projectName and environment MDC property`() {
        testComponent.withOrganizationIdAndProjectIdAndEnvironmentId(ORGANIZATION_ID, PROJECT_ID, ENVIRONMENT_ID)
        assertThat(testComponent.value).isEqualTo("5")
        Mockito.verify(organizationService).searchById(ORGANIZATION_ID)
        Mockito.verify(projectService).searchById(PROJECT_ID)
        Mockito.verify(environmentService).searchById(ENVIRONMENT_ID)
    }

    @Test
    internal fun `should set organizationName and projectName and environment and featureFLag uuid MDC property`() {
        testComponent.withOrganizationIdAndProjectIdAndEnvironmentIdAndFeatureFlag(
                ORGANIZATION_ID, PROJECT_ID, ENVIRONMENT_ID, FEATURE_FLAG
        )
        assertThat(testComponent.value).isEqualTo("6")
        Mockito.verify(organizationService).searchById(ORGANIZATION_ID)
        Mockito.verify(projectService).searchById(PROJECT_ID)
        Mockito.verify(environmentService).searchById(ENVIRONMENT_ID)
    }
}

@RestController
class TestComponent(var value: String = "") {

    fun withOrganizationId(@OrganizationId organizationId: Long) {
        value = "1"
        assertThat(MDC.get("organizationName")).isEqualTo("orgName")
    }

    fun withProjectId(@ProjectId projectId: Long) {
        value = "2"
        assertThat(MDC.get("projectName")).isEqualTo("project")
    }

    fun withEnvironmentId(@EnvironmentId environmentId: Long) {
        value = "3"
        assertThat(MDC.get("environmentName")).isEqualTo("env")
    }

    fun withOrganizationIdAndProjectId(@OrganizationId organizationId: Long, @ProjectId projectId: Long) {
        value = "4"
        assertThat(MDC.get("organizationName")).isEqualTo("orgName")
        assertThat(MDC.get("projectName")).isEqualTo("project")
    }

    fun withOrganizationIdAndProjectIdAndEnvironmentId(@OrganizationId organizationId: Long,
                                                       @ProjectId projectId: Long,
                                                       @EnvironmentId environmentId: Long) {
        value = "5"
        assertThat(MDC.get("organizationName")).isEqualTo("orgName")
        assertThat(MDC.get("projectName")).isEqualTo("project")
        assertThat(MDC.get("environmentName")).isEqualTo("env")
    }

    fun withOrganizationIdAndProjectIdAndEnvironmentIdAndFeatureFlag(
            @OrganizationId organizationId: Long,
            @ProjectId projectId: Long,
            @EnvironmentId environmentId: Long,
            @FeatureFlagId featureFlagId: String) {
        value = "6"
        assertThat(MDC.get("organizationName")).isEqualTo("orgName")
        assertThat(MDC.get("projectName")).isEqualTo("project")
        assertThat(MDC.get("environmentName")).isEqualTo("env")
        assertThat(MDC.get("featureFLagUUID")).isEqualTo(featureFlagId)
    }


}