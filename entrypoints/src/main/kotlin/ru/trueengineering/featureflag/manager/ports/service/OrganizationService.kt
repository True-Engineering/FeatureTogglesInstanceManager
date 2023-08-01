package ru.trueengineering.featureflag.manager.ports.service

import org.springframework.stereotype.Service
import ru.trueengineering.featureflag.manager.core.domen.organization.CreateOrganizationCommand
import ru.trueengineering.featureflag.manager.core.domen.organization.CreateOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationCommand
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationUserCommand
import ru.trueengineering.featureflag.manager.core.domen.organization.DeleteOrganizationUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.FetchAllOrganizationsUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.FetchMembersForOrganizationQuery
import ru.trueengineering.featureflag.manager.core.domen.organization.FetchMembersForOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.SearchOrganizationByIdQuery
import ru.trueengineering.featureflag.manager.core.domen.organization.SearchOrganizationUseCase
import ru.trueengineering.featureflag.manager.ports.rest.controller.OrganizationDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.OrganizationUsersDto
import ru.trueengineering.featureflag.manager.ports.service.mapper.OrganizationMapper
import ru.trueengineering.featureflag.manager.ports.service.mapper.OrganizationUserMapper

@Service
class OrganizationService(
        private val organizationMapper: OrganizationMapper,
        private val organizationUserMapper: OrganizationUserMapper,
        private val createOrganizationUseCase: CreateOrganizationUseCase,
        private val deleteOrganizationUseCase: DeleteOrganizationUseCase,
        private val searchOrganizationUseCase: SearchOrganizationUseCase,
        private val fetchAllOrganizationsUseCase: FetchAllOrganizationsUseCase,
        private val fetchMembersForOrganizationUseCase: FetchMembersForOrganizationUseCase,
        private val deleteOrganizationUserUseCase: DeleteOrganizationUserUseCase
) {

    fun create(command: CreateOrganizationCommand) =
            organizationMapper.convertToDto(createOrganizationUseCase.execute(command)).apply {
                membersCount = fetchMembers(id)
            }

    fun delete(command: DeleteOrganizationCommand) =
            deleteOrganizationUseCase.execute(command)

    fun searchById(organizationId: Long) =
            organizationMapper.convertToDto(searchOrganizationUseCase.search(SearchOrganizationByIdQuery(organizationId)))
                    .apply {
                        membersCount = fetchMembers(organizationId)
                    }

    fun fetchAll(): List<OrganizationDto> {
        return organizationMapper.convertToDtoList(fetchAllOrganizationsUseCase.search()).onEach {
            it.membersCount = fetchMembers(it.id)
        }
    }

    fun getMembers(organizationId: Long): OrganizationUsersDto {
        val users = fetchMembersForOrganizationUseCase.search(FetchMembersForOrganizationQuery(organizationId))
        return OrganizationUsersDto(organizationUserMapper.convertToDtoList(users))
    }

    fun deleteUser(userId: Long, organizationId: Long) {
        deleteOrganizationUserUseCase.execute(DeleteOrganizationUserCommand(userId, organizationId))
    }

    fun fetchMembers(organizationId: Long): Int? {
        return try {
            fetchMembersForOrganizationUseCase.searchMembersCount(
                    FetchMembersForOrganizationQuery(organizationId))
        } catch (e: Exception) {
            null
        }
    }

}