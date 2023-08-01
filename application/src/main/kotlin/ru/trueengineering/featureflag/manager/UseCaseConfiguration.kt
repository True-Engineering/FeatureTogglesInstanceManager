package ru.trueengineering.featureflag.manager

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.authorization.RoleDefiner
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchCurrentUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUserUseCase
import ru.trueengineering.featureflag.manager.core.impl.changes.ChangesHistoryFacade
import ru.trueengineering.featureflag.manager.core.impl.changes.ChangesHistoryRepository
import ru.trueengineering.featureflag.manager.core.impl.environment.EnvironmentFacade
import ru.trueengineering.featureflag.manager.core.impl.environment.EnvironmentRepository
import ru.trueengineering.featureflag.manager.core.impl.environment.InstanceService
import ru.trueengineering.featureflag.manager.core.impl.environment.scheduler.DelayUnfreezeService
import ru.trueengineering.featureflag.manager.core.impl.organization.OrganizationFacade
import ru.trueengineering.featureflag.manager.core.impl.organization.OrganizationRepository
import ru.trueengineering.featureflag.manager.core.impl.project.ProjectFacade
import ru.trueengineering.featureflag.manager.core.impl.project.ProjectRepository
import ru.trueengineering.featureflag.manager.core.impl.toggle.FeatureFlagFacade
import ru.trueengineering.featureflag.manager.core.impl.toggle.FeatureFlagPropertyHelper
import ru.trueengineering.featureflag.manager.core.impl.toggle.FeatureFlagRepository
import ru.trueengineering.featureflag.manager.core.impl.user.FeatureFlagPortalRoleDefiner
import ru.trueengineering.featureflag.manager.core.impl.user.InvitationFacade
import ru.trueengineering.featureflag.manager.core.impl.user.InvitationRepository
import ru.trueengineering.featureflag.manager.core.impl.user.UserFacade
import ru.trueengineering.featureflag.manager.core.impl.user.UserRepository
import ru.trueengineering.featureflag.manager.core.impl.validator.CreateFeatureFlagCommandValidatorHandler
import ru.trueengineering.featureflag.manager.core.impl.validator.EditFeatureFlagCommandValidatorHandler
import ru.trueengineering.featureflag.manager.core.impl.validator.ProjectValidatorHandler

@Configuration
class UseCaseConfiguration {

    @Bean
    fun organizationFacade(
        @Autowired repository: OrganizationRepository,
        @Autowired fetchCurrentUserUseCase: FetchCurrentUserUseCase,
        @Autowired featureFlagRepository: FeatureFlagRepository,
        @Autowired fetchMembersForProjectUseCase: FetchMembersForProjectUseCase,
        @Autowired permissionService: IPermissionService,
        @Autowired fetchUserUseCase: FetchUserUseCase,
        @Autowired projectValidatorHandler: ProjectValidatorHandler
    )
            : OrganizationFacade = OrganizationFacade(
        repository,
        featureFlagRepository,
        fetchMembersForProjectUseCase,
        permissionService,
        fetchUserUseCase,
        projectValidatorHandler
    )

    @Bean
    fun projectFacade(
        @Autowired repository: ProjectRepository,
        @Autowired organizationRepository: OrganizationRepository,
        @Autowired featureFlagRepository: FeatureFlagRepository,
        @Autowired permissionService: IPermissionService,
        @Autowired fetchUserUseCase: FetchUserUseCase,
        @Autowired fetchCurrentUserUseCase: FetchCurrentUserUseCase,
        @Autowired projectValidatorHandler: ProjectValidatorHandler
    ): ProjectFacade = ProjectFacade(
        repository,
        organizationRepository,
        featureFlagRepository,
        permissionService,
        fetchUserUseCase,
        fetchCurrentUserUseCase,
        projectValidatorHandler
    )

    @Bean
    fun environmentFacade(
        @Autowired repository: EnvironmentRepository,
        @Autowired featureFlagRepository: FeatureFlagRepository,
        @Autowired projectRepository: ProjectRepository,
        @Autowired changesHistoryFacade: ChangesHistoryFacade,
        @Autowired userFacade: UserFacade,
        @Autowired delayUnfreezeService: DelayUnfreezeService,
    ): EnvironmentFacade = EnvironmentFacade(
        repository,
        featureFlagRepository,
        projectRepository,
        userFacade,
        delayUnfreezeService
    )

    @Bean
    fun changesHistoryFacade(
        @Autowired projectFacade: ProjectFacade,
        @Autowired userFacade: UserFacade,
        @Autowired environmentRepository: EnvironmentRepository,
        @Autowired featureFlagRepository: FeatureFlagRepository,
        @Autowired changesHistoryRepository: ChangesHistoryRepository
    ): ChangesHistoryFacade = ChangesHistoryFacade(
        projectFacade,
        userFacade,
        environmentRepository,
        featureFlagRepository,
        changesHistoryRepository
    )

    @Bean
    fun featureFlagFacade(
        @Autowired environmentFacade: EnvironmentFacade,
        @Autowired projectFacade: ProjectFacade,
        @Autowired featureFlagRepository: FeatureFlagRepository,
        @Autowired changesHistoryFacade: ChangesHistoryFacade,
        @Autowired featureFlagPropertyHelper: FeatureFlagPropertyHelper,
        @Autowired createFeatureFlagCommandValidatorHandler: CreateFeatureFlagCommandValidatorHandler,
        @Autowired editFeatureFlagCommandValidatorHandler: EditFeatureFlagCommandValidatorHandler
    ): FeatureFlagFacade = FeatureFlagFacade(
        environmentFacade,
        projectFacade,
        featureFlagRepository,
        featureFlagPropertyHelper,
        createFeatureFlagCommandValidatorHandler,
        editFeatureFlagCommandValidatorHandler
    )

    @Bean
    fun invitationFacade(
        @Autowired invitationRepository: InvitationRepository,
        @Autowired permissionService: IPermissionService,
        @Autowired fetchCurrentUserUseCase: FetchCurrentUserUseCase,
        @Autowired organizationRepository: OrganizationRepository
    ) = InvitationFacade(invitationRepository, permissionService, fetchCurrentUserUseCase, organizationRepository)


    @Bean
    fun instanceService(
        @Autowired repository: EnvironmentRepository,
        @Value("\${featureFlag.instance.outOfSyncPeriod}") instanceOutOfSyncPeriod: String
    ): InstanceService = InstanceService(repository, instanceOutOfSyncPeriod.toInt())

    @Bean
    fun userFacade(@Autowired userRepository: UserRepository): UserFacade = UserFacade(userRepository)

    @Bean
    @ConditionalOnMissingBean
    fun projectRoleDefiner(): RoleDefiner {
        return FeatureFlagPortalRoleDefiner()
    }
}