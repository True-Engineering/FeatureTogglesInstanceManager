package ru.trueengineering.featureflag.manager.core.impl.project

import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.auth.UserPermissions
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.authorization.annotation.AspectUtils.executeWithAdminRole
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.EDIT
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.PENDING_APPROVE
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission.READ_PROJECT
import ru.trueengineering.featureflag.manager.core.domen.environment.AddNewEnvironmentToProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.environment.CreateEnvironmentCommand
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteUserFromProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.project.DeleteUserFromProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.EnvironmentRole
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectQuery
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectUser
import ru.trueengineering.featureflag.manager.core.domen.project.SearchProjectByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.project.SearchProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.UpdateProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.project.UpdateProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.ActivateUserCommand
import ru.trueengineering.featureflag.manager.core.domen.user.ActivateUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.EditProjectRoleUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.EditUserProjectRoleCommand
import ru.trueengineering.featureflag.manager.core.domen.user.FetchCurrentUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUsersByEmailListQuery
import ru.trueengineering.featureflag.manager.core.domen.user.UpdateUserEnvironmentRoleCommand
import ru.trueengineering.featureflag.manager.core.domen.user.UpdateUserEnvironmentRoleUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.domen.user.UserRole
import ru.trueengineering.featureflag.manager.core.domen.user.UserStatus
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.core.impl.organization.OrganizationRepository
import ru.trueengineering.featureflag.manager.core.impl.toggle.FeatureFlagRepository
import ru.trueengineering.featureflag.manager.core.impl.validator.ProjectValidatorHandler

open class ProjectFacade(
        private val projectRepository: ProjectRepository,
        private val organizationRepository: OrganizationRepository,
        private val featureFlagRepository: FeatureFlagRepository,
        private val permissionService: IPermissionService,
        private val fetchUserUseCase: FetchUserUseCase,
        private val fetchCurrentUserUseCase: FetchCurrentUserUseCase,
        private val projectValidatorHandler: ProjectValidatorHandler
) : AddNewEnvironmentToProjectUseCase, DeleteProjectUseCase,
    SearchProjectUseCase, UpdateProjectUseCase,
        ActivateUserUseCase, UpdateUserEnvironmentRoleUseCase, FetchMembersForProjectUseCase,
    DeleteUserFromProjectUseCase, EditProjectRoleUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun execute(command: DeleteProjectCommand): Any {
        return projectRepository.deleteProject(command.projectId)
    }

    @Transactional
    override fun execute(command: ActivateUserCommand) {
        val user = fetchUserUseCase.searchById(command.userId) ?: throw ServiceException(
            ErrorCode.USER_NOT_FOUND,
            "User with id = ${command.userId} not found!"
        )

        val project = projectRepository.getById(command.projectId)
        return if (permissionService.getUserRoleForEntity(project, user) != CustomRole.NO_ACCESS) {
            log.debug("User ${user.userName} is already activated in project ${project.name}")
        } else if (permissionService.isGrantedPermission(project, PENDING_APPROVE, user)) {
            executeWithAdminRole {
                permissionService.clearPermissionsForUser(project, user)
                permissionService.grantPermissionsForUser(project, CustomRole.MEMBER.projectPermissions.toList(), user)
            }
            return
        } else {
            throw ServiceException(ErrorCode.USER_IS_NOT_INVITED_TO_PROJECT)
        }
    }

    override fun search(query: SearchProjectByIdQuery): Project {
        val project = projectRepository.getById(query.projectId)

        return project.apply {
            featureFlagsCount = featureFlagRepository.getFeatureFlagsCountForProject(id!!)
            membersCount = search(FetchMembersForProjectQuery(query.projectId)).size.toLong()

        }
    }

    @Transactional(rollbackFor = [ServiceException::class])
    override fun execute(command: UpdateProjectCommand): Project {
        projectRepository.updateName(command.projectId, command.projectName)
        projectRepository.setProperties(command.projectId, command.properties)

        val project = projectRepository.getById(command.projectId)

        return projectValidatorHandler.validateOrThrow(project)
    }

    override fun execute(command: CreateEnvironmentCommand): Environment {
        projectRepository.addNewEnvironment(command.projectId, Environment(name = command.name))
        return projectRepository
            .getById(command.projectId).environments.first { it.name == command.name }
    }

    @Transactional
    override fun execute(command: UpdateUserEnvironmentRoleCommand): ProjectUser {
        val user = fetchUserUseCase.searchById(command.userId) ?: throw ServiceException(
            ErrorCode.USER_NOT_FOUND,
            "User with id = ${command.userId} not found!"
        )

        val project = projectRepository.getById(command.projectId)
        val environment = project.environments.firstOrNull { it.id == command.environmentId }
            ?: throw ServiceException(
                ErrorCode.ENVIRONMENT_NOT_FOUND,
                "Unable to find environment ${command.environmentId}"
            )

        permissionService.clearPermissionsForUser(environment, user)

        val permissions = UserRole.getEnvironmentPermissionByRole(command.role)
        if (permissions.isNotEmpty()) permissionService.grantPermissionsForUser(environment, permissions, user)
        return gerProjectUser(
            project,
            user,
            permissionService.getPermissionsForUser(project.environments.plus(project), user)
        )
    }

    override fun search(command: FetchMembersForProjectQuery): List<ProjectUser> {
        val project = projectRepository.getById(command.projectId)
        val projectUsersEmails = permissionService
            .getUsersByEntity(
                project,
                listOf(PENDING_APPROVE, READ_PROJECT, EDIT)
            )
        val users = fetchUserUseCase.execute(FetchUsersByEmailListQuery(projectUsersEmails.toList()))
        val environments = project.environments

        val businessEntities: ArrayList<BusinessEntity> = arrayListOf(project)
        businessEntities.addAll(environments)
        val permissionsForUsers = permissionService.getPermissionsForUsers(businessEntities, users)
        val usersPermissions = permissionsForUsers
            .associateBy({ it.userName }, { it })

        return users.map {
            gerProjectUser(project, it, usersPermissions[it.email])
        }
    }

    private fun gerProjectUser(
            project: Project,
            user: User,
            userPermissions: UserPermissions?
    ): ProjectUser {
        val environmentPermission = project.environments.map { env ->
            EnvironmentRole(env.id, env.name, defineEnvironmentPermission(env, userPermissions))
        }
        val projectPermissions = userPermissions?.findByObjectIdentity(project)?.permissions ?: emptyList()
        val isPending = projectPermissions.any { permission -> PENDING_APPROVE.isSame(permission) }
        user.status = if (isPending) UserStatus.PENDING else UserStatus.ACTIVE
        return ProjectUser(
            project.id!!,
            project.name,
            user,
            permissionService.getUserRoleForEntity(project, user),
            environmentPermission
        )
    }

    @Transactional
    override fun execute(command: EditUserProjectRoleCommand): ProjectUser {
        val user = fetchUserUseCase.searchById(command.userId) ?: throw ServiceException(
            ErrorCode.USER_NOT_FOUND,
            "User with id = ${command.userId} not found!"
        )

        val organization = organizationRepository.findById(command.organizationId)
        val project = organization
            .projects.find { it.id == command.projectId } ?: throw ServiceException(ErrorCode.PROJECT_NOT_FOUND)

        val oldUserRole = permissionService.getUserRoleForEntity(project, user)
        if (oldUserRole == command.projectRole) {
            log.debug("User ${user.userName} already has role ${command.projectRole} in project ${project.name}")
            return gerProjectUser(
                project,
                user,
                permissionService.getPermissionsForUser(project.environments.plus(project), user)
            )
        }
        if (oldUserRole == CustomRole.ADMIN
            && (!permissionService.isGrantedPermissionForCurrentUser(organization, CustomPermission.EDIT_MEMBERS) ||
                user.id != fetchCurrentUserUseCase.search().id)
        ) {
            // роль админа в проекте может поменять только участник организации с соответствующим правом,
            // либо сам пользователь для себя
            throw org.springframework.security.access.AccessDeniedException(
                "You do not have permissions " +
                        "for changing role for user"
            )
        }

        // отбираем текущие права на проект и окружения
        permissionService.clearPermissionsForUser(project, user)

        // назначаем новые права в проекте согласно новой роли
        val newProjectRole = command.projectRole
        permissionService.grantPermissionsForUser(
            project,
            newProjectRole.projectPermissions.plus(newProjectRole.environmentPermissions).toList(),
            user
        )
        if (newProjectRole.needClearLowLayerEntityPermissions) {
            // если необходимо обновляем права для окружений
            project.environments
                .forEach { permissionService.clearPermissionsForUser(it, user) }
        }
        return gerProjectUser(
            project,
            user,
            permissionService.getPermissionsForUser(project.environments.plus(project), user)
        )
    }

    @Transactional
    override fun execute(command: DeleteUserFromProjectCommand) {
        val project = projectRepository.getById(command.projectId)
        val user = fetchUserUseCase.searchById(command.userId) ?: throw ServiceException(ErrorCode.USER_NOT_FOUND)
        listOf<BusinessEntity>(project).plus(project.environments)
            .forEach { permissionService.clearPermissionsForUser(it, user) }
    }

    private fun defineEnvironmentPermission(
        environment: Environment,
        permissions: UserPermissions?
    ): UserRole {
        val envPermissions = permissions?.findByObjectIdentity(environment)?.permissions ?: emptyList()
        return UserRole.getRoleByPermissions(envPermissions)
    }
}