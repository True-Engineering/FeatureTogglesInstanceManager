package ru.trueengineering.featureflag.manager.core.impl.organization

import org.springframework.transaction.annotation.Transactional
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.authorization.config.CustomPermission
import ru.trueengineering.featureflag.manager.core.domen.organization.CreateOrganizationCommand
import ru.trueengineering.featureflag.manager.core.domen.organization.CreateOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationCommand
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationUserCommand
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.FetchAllOrganizationsUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.FetchMembersForOrganizationQuery
import ru.trueengineering.featureflag.manager.core.domen.organization.FetchMembersForOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.Organization
import ru.trueengineering.featureflag.manager.core.domen.organization.OrganizationUser
import ru.trueengineering.featureflag.manager.core.domen.organization.SearchOrganizationByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.organization.SearchOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.AddNewProjectToOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.CreateProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectQuery
import ru.trueengineering.featureflag.manager.core.domen.project.FetchMembersForProjectUseCase
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUsersByEmailListQuery
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.impl.validator.ProjectValidatorHandler
import ru.trueengineering.featureflag.manager.core.impl.toggle.FeatureFlagRepository

open class OrganizationFacade(
    private val organizationRepository: OrganizationRepository,
    private val featureFlagRepository: FeatureFlagRepository,
    private val fetchMembersForProjectUseCase: FetchMembersForProjectUseCase,
    private val permissionService: IPermissionService,
    private val fetchUserUseCase: FetchUserUseCase,
    private val projectValidatorHandler: ProjectValidatorHandler
) :
    AddNewProjectToOrganizationUseCase,
    CreateOrganizationUseCase, DeleteOrganizationUseCase, FetchAllOrganizationsUseCase, SearchOrganizationUseCase,
    FetchMembersForOrganizationUseCase, DeleteOrganizationUserUseCase {

    override fun execute(command: CreateOrganizationCommand): Organization {
        return organizationRepository.create(command.name)
    }

    override fun execute(command: DeleteOrganizationCommand) {
        organizationRepository.findById(command.organizationId)
        organizationRepository.removeById(command.organizationId)
    }

    @Transactional
    override fun search(): List<Organization> {
        return organizationRepository.findAll().onEach { it ->
            it.projects.forEach { project -> fillProjectStatistics(project) }
        }
    }

    override fun search(query: SearchOrganizationByIdQuery): Organization {
        val organization = organizationRepository.findById(query.organizationId)
        return organization.apply {
            projects.forEach { fillProjectStatistics(it) }
        }
    }

    override fun execute(command: CreateProjectCommand): Project {
        val project = Project(name = command.projectName, properties = command.properties)
        projectValidatorHandler.validateOrThrow(project)

        val newProject = organizationRepository.addNewProject(command.organizationId, project)
        fillProjectStatistics(newProject)

        return newProject
    }

    private fun fillProjectStatistics(project: Project) {
        project.featureFlagsCount = featureFlagRepository.getFeatureFlagsCountForProject(project.id!!)
        if (permissionService.isGrantedPermissionForCurrentUser(project, CustomPermission.READ_MEMBERS)) {
            project.membersCount =
                fetchMembersForProjectUseCase.search(FetchMembersForProjectQuery(project.id!!)).size.toLong()
        }
    }

    override fun search(query: FetchMembersForOrganizationQuery): List<OrganizationUser> {
        val organization = organizationRepository.findById(query.organizationId)
        val users = getOrganizationUsers(organization)

        val projectUsers = organization.projects.flatMap { project ->
            fetchMembersForProjectUseCase.search(FetchMembersForProjectQuery(project.id!!)) }
            .groupBy({ it.user }, { it })

        return users.map { OrganizationUser(it, projectUsers[it] ?: emptyList()) }
    }

    override fun searchMembersCount(query: FetchMembersForOrganizationQuery): Int? {
        return try {
            val organization = organizationRepository.findById(query.organizationId)
            val organizationUsersSids = permissionService.getUsersByEntity(organization,
                listOf(CustomPermission.READ_ORGANIZATION, CustomPermission.EDIT))
            fetchUserUseCase.searchUserCount(FetchUsersByEmailListQuery(organizationUsersSids.toList()))
        } catch (ignored: Exception) {
            null
        }
    }

    private fun getOrganizationUsers(organization: Organization): List<User> {
        val organizationUsersSids = permissionService.getUsersByEntity(organization,
            listOf(CustomPermission.READ_ORGANIZATION, CustomPermission.EDIT))
        return fetchUserUseCase.execute(FetchUsersByEmailListQuery(organizationUsersSids.toList()))
    }

    @Transactional
    override fun execute(command: DeleteOrganizationUserCommand) {
        val organization = organizationRepository.findById(command.organizationId)
        val user = fetchUserUseCase.searchById(command.userId) ?: return

        organization.projects.forEach {
            it.environments.forEach {
                env -> permissionService.clearPermissionsForUser(env, user)
            }
            permissionService.clearPermissionsForUser(it, user)
        }
        permissionService.clearPermissionsForUser(organization, user)

    }
}