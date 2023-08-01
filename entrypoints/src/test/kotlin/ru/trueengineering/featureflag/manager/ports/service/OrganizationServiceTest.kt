package ru.trueengineering.featureflag.manager.ports.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
import ru.trueengineering.featureflag.manager.ports.rest.controller.OrganizationDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.OrganizationUserDto
import ru.trueengineering.featureflag.manager.ports.service.mapper.OrganizationMapper
import ru.trueengineering.featureflag.manager.ports.service.mapper.OrganizationUserMapper

internal class OrganizationServiceTest {

    private val organizationMapper: OrganizationMapper = mockk()
    private val organizationUserMapper: OrganizationUserMapper = mockk()
    private val createOrganizationUseCase: CreateOrganizationUseCase = mockk()
    private val deleteOrganizationUseCase: DeleteOrganizationUseCase = mockk()
    private val searchOrganizationUseCase: SearchOrganizationUseCase = mockk()
    private val fetchAllOrganizationsUseCase: FetchAllOrganizationsUseCase = mockk()
    private val fetchMembersForOrganizationUseCase: FetchMembersForOrganizationUseCase = mockk()
    private val deleteOrganizationUserUseCase: DeleteOrganizationUserUseCase = mockk()

    private val uut: OrganizationService = OrganizationService(
        organizationMapper,
        organizationUserMapper,
        createOrganizationUseCase,
        deleteOrganizationUseCase,
        searchOrganizationUseCase,
        fetchAllOrganizationsUseCase,
        fetchMembersForOrganizationUseCase,
        deleteOrganizationUserUseCase)

    private val organizationOne = Organization(1, "org1")
    private val organizationTwo = Organization(2, "org2")

    private val organizationDtoOne = OrganizationDto(1, "org1")
    private val organizationDtoTwo = OrganizationDto(2, "org2")

    @BeforeEach
    fun setUp() {
        every { organizationMapper.convertToDto(organizationOne) } returns organizationDtoOne
        every { organizationMapper.convertToDtoList(listOf(organizationOne, organizationTwo)) } returns
            listOf(organizationDtoOne, organizationDtoTwo)
        every { fetchMembersForOrganizationUseCase.searchMembersCount(FetchMembersForOrganizationQuery(1)) } returns 15
        every { fetchMembersForOrganizationUseCase.searchMembersCount(FetchMembersForOrganizationQuery(2)) } returns 20
    }

    @Test
    fun create() {
        every { createOrganizationUseCase.execute(CreateOrganizationCommand("org")) } returns organizationOne
        val actual = uut.create(CreateOrganizationCommand("org"))
        assertThat(actual).isEqualTo(organizationDtoOne)
    }

    @Test
    fun delete() {
        every { deleteOrganizationUseCase.execute(DeleteOrganizationCommand(1)) } just Runs
        uut.delete(DeleteOrganizationCommand(1))
        verify { deleteOrganizationUseCase.execute(DeleteOrganizationCommand(1)) }
    }

    @Test
    fun searchById() {
        every { searchOrganizationUseCase.search(SearchOrganizationByIdQuery(1)) } returns organizationOne
        val actual = uut.searchById(1)
        assertThat(actual).isEqualTo(organizationDtoOne)
        assertThat(actual.membersCount).isEqualTo(15)

    }

    @Test
    fun fetchAll() {
        every { fetchAllOrganizationsUseCase.search() } returns listOf(organizationOne, organizationTwo)
        val actual = uut.fetchAll()
        assertThat(actual).hasSize(2).containsExactlyInAnyOrder(organizationDtoOne, organizationDtoTwo)
        assertThat(actual)
            .extracting<Int> { it.membersCount }
            .containsExactly(15, 20)
    }

    @Test
    fun getMembers() {
        val organizationUser = OrganizationUser(mockk(), emptyList())
        every { fetchMembersForOrganizationUseCase.search(FetchMembersForOrganizationQuery(1)) } returns listOf(organizationUser)
        val organizationUserDto = OrganizationUserDto(mockk(), emptyList())
        every { organizationUserMapper.convertToDtoList(listOf(organizationUser)) } returns listOf(organizationUserDto)
        val actual = uut.getMembers(1L)
        assertThat(actual.users).hasSize(1).containsExactly(organizationUserDto)
    }

    @Test
    fun deleteUser() {
        every { deleteOrganizationUserUseCase.execute(DeleteOrganizationUserCommand(1, 1)) } just Runs
        uut.deleteUser(1, 1)
        verify { deleteOrganizationUserUseCase.execute(DeleteOrganizationUserCommand(1, 1)) }
    }
}