package ru.trueengineering.featureflag.manager.ports

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.trueengineering.featureflag.manager.core.domen.changes.CreateChangesHistoryRecordUseCase
import ru.trueengineering.featureflag.manager.core.domen.changes.GetProjectChangesHistoryUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.AddNewEnvironmentToProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.CreateEnvironmentTokenUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.DeleteInstanceUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.FetchAllEnvironmentsOfProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.FreezeEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.GetCompareEnvironmentsStateUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.SearchEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.UnfreezeEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.UpdateFlagsStateUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.CreateOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.FetchAllOrganizationsUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.FetchMembersForOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.SearchOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.AddNewProjectToOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteUserFromProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.SearchProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.UpdateProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.CreateFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.DeleteFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.DisableFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagStrategyUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.EnableFeatureFlagUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllEnabledFeatureFlagsForEnvironmentUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForAgentUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsForProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FetchAllFeatureFlagsTagsUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.FindFeatureFlagByPatternUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportChangesUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.GetImportEnvironmentsUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizeEnvironmentsUseCase
import ru.trueengineering.featureflag.manager.core.domen.toggle.SynchronizePortalsUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.ActivateUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.CreateUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.EditProjectRoleUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchCurrentUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchInvitationUserCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.InviteUserToProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.SetDefaultProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.UpdateUserEnvironmentRoleUseCase
import ru.trueengineering.featureflag.manager.ports.config.EntryPointsConfiguration
import ru.trueengineering.featureflag.manager.ports.service.ChangesHistoryService
import ru.trueengineering.featureflag.manager.ports.service.EnvironmentService
import ru.trueengineering.featureflag.manager.ports.service.FeatureFlagService
import ru.trueengineering.featureflag.manager.ports.service.OrganizationService
import ru.trueengineering.featureflag.manager.ports.service.ProjectService
import ru.trueengineering.featureflag.manager.ports.service.UserService

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = arrayOf(EntryPointsConfiguration::class))
internal class EntryPointsConfigurationTest {
    @MockBean
    lateinit var deleteOrganizationUseCase: DeleteOrganizationUseCase
    @MockBean
    lateinit var createOrganizationUseCase: CreateOrganizationUseCase
    @MockBean
    lateinit var searchOrganizationUseCase: SearchOrganizationUseCase
    @MockBean
    lateinit var fetchAllOrganizationsUseCase: FetchAllOrganizationsUseCase
    @MockBean
    lateinit var addNewProjectToOrganizationUseCase: AddNewProjectToOrganizationUseCase
    @MockBean
    lateinit var deleteProjectUseCase: DeleteProjectUseCase
    @MockBean
    lateinit var searchProjectByIdUseCase: SearchProjectUseCase
    @MockBean
    lateinit var fetchAllFeatureFlagsForAgentUseCase: FetchAllFeatureFlagsForAgentUseCase
    @MockBean
    lateinit var deleteUseCase: DeleteEnvironmentUseCase
    @MockBean
    lateinit var createUseCase: AddNewEnvironmentToProjectUseCase
    @MockBean
    lateinit var updateUseCase: UpdateEnvironmentUseCase
    @MockBean
    lateinit var searchUseCase: SearchEnvironmentUseCase
    @MockBean
    lateinit var createEnvironmentTokenUseCase: CreateEnvironmentTokenUseCase
    @MockBean
    lateinit var fetchAllUseCase: FetchAllEnvironmentsOfProjectUseCase
    @MockBean
    lateinit var createFeatureFlagUseCase: CreateFeatureFlagUseCase
    @MockBean
    lateinit var fetchAllFeatureFlagsForProjectUseCase: FetchAllFeatureFlagsForProjectUseCase
    @MockBean
    lateinit var enableFeatureFlagUseCase: EnableFeatureFlagUseCase
    @MockBean
    lateinit var disableFeatureFlagUseCase: DisableFeatureFlagUseCase
    @MockBean
    lateinit var deleteFeatureFlagUseCase: DeleteFeatureFlagUseCase
    @MockBean
    lateinit var updateProjectUseCase: UpdateProjectUseCase
    @MockBean
    lateinit var deleteInstanceUseCase: DeleteInstanceUseCase
    @MockBean
    lateinit var fetchCurrentUserUseCase: FetchCurrentUserUseCase
    @MockBean
    lateinit var fetchUserUseCase: FetchUserUseCase
    @MockBean
    lateinit var activateUserUseCase: ActivateUserUseCase
    @MockBean
    lateinit var createUserUseCase: CreateUserUseCase
    @MockBean
    lateinit var fetchInvitationUserCase: FetchInvitationUserCase
    @MockBean
    lateinit var setDefaultProjectUseCase: SetDefaultProjectUseCase
    @MockBean
    lateinit var inviteUserToProjectUseCase: InviteUserToProjectUseCase
    @MockBean
    lateinit var editFeatureFlagUseCase: EditFeatureFlagUseCase
    @MockBean
    lateinit var editFeatureFlagStrategyUseCase: EditFeatureFlagStrategyUseCase
    @MockBean
    lateinit var updateUserEnvironmentRoleUseCase: UpdateUserEnvironmentRoleUseCase
    @MockBean
    lateinit var fetchMembersForProjectUseCase: FetchMembersForProjectUseCase
    @MockBean
    lateinit var fetchMembersForOrganizationUseCase: FetchMembersForOrganizationUseCase
    @MockBean
    lateinit var deleteUserFromProjectUseCase: DeleteUserFromProjectUseCase
    @MockBean
    lateinit var editProjectRoleUseCase: EditProjectRoleUseCase
    @MockBean
    lateinit var deleteOrganizationUserUseCase: DeleteOrganizationUserUseCase
    @MockBean
    lateinit var synchronizePortalsUseCase: SynchronizePortalsUseCase
    @MockBean
    lateinit var synchronizeEnvironmentsUseCase: SynchronizeEnvironmentsUseCase
    @MockBean
    lateinit var objectMapper: ObjectMapper
    @MockBean
    lateinit var importChangesUseCase: GetImportChangesUseCase
    @MockBean
    lateinit var importEnvironmentsUseCase: GetImportEnvironmentsUseCase
    @MockBean
    lateinit var createChangesHistoryRecordUseCase: CreateChangesHistoryRecordUseCase
    @MockBean
    lateinit var getAllProjectChangesUseCase: GetProjectChangesHistoryUseCase
    @MockBean
    lateinit var getCompareEnvironmentsStateUseCase: GetCompareEnvironmentsStateUseCase
    @MockBean
    lateinit var updateFlagsStateUseCase: UpdateFlagsStateUseCase
    @MockBean
    lateinit var fetchAllEnabledFeatureFlagsForEnvironmentUseCase: FetchAllEnabledFeatureFlagsForEnvironmentUseCase
    @MockBean
    lateinit var findFeatureFlagByPatternUseCase: FindFeatureFlagByPatternUseCase
    @MockBean
    lateinit var fetchAllFeatureFlagsTagsUseCase: FetchAllFeatureFlagsTagsUseCase
    @MockBean
    lateinit var freezeEnvironmentUseCase: FreezeEnvironmentUseCase
    @MockBean
    lateinit var unfreezeEnvironmentUseCase: UnfreezeEnvironmentUseCase

    @Autowired
    lateinit var environmentService: EnvironmentService
    @Autowired
    lateinit var featureFlagService: FeatureFlagService
    @Autowired
    lateinit var organizationService: OrganizationService
    @Autowired
    lateinit var projectService: ProjectService
    @Autowired
    lateinit var userService: UserService
    @Autowired
    lateinit var changesHistoryService: ChangesHistoryService

    @Test
    internal fun `should start up configuration`() {
        assertTrue(true)
        assertNotNull(environmentService)
        assertNotNull(featureFlagService)
        assertNotNull(organizationService)
        assertNotNull(projectService)
        assertNotNull(userService)
        assertNotNull(changesHistoryService)
    }
}
