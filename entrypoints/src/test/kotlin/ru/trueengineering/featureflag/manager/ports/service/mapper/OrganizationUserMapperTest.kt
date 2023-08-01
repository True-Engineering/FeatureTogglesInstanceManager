package ru.trueengineering.featureflag.manager.ports.service.mapper

import org.springframework.beans.factory.annotation.Autowired
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.core.domen.organization.OrganizationUser
import ru.trueengineering.featureflag.manager.core.domen.project.EnvironmentRole
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectUser
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.domen.user.UserRole
import ru.trueengineering.featureflag.manager.ports.rest.controller.OrganizationUserDto
import kotlin.test.assertEquals

class OrganizationUserMapperTest(
    @Autowired override var uut: OrganizationUserMapper
) : MapperSpec<OrganizationUserMapper, OrganizationUser, OrganizationUserDto> {

    private val user = User("user", "email", 100L)

    override fun verifyDto(actualDto: OrganizationUserDto) {
        assertEquals(user.id, actualDto.user.id)
        assertEquals(1, actualDto.projects.size)
        assertEquals(12L, actualDto.projects[0].projectId)
    }

    override fun buildDomain(): OrganizationUser {

        return OrganizationUser(
            user,
            listOf(
                ProjectUser(12L, "project", user, CustomRole.ADMIN, listOf(
                    EnvironmentRole(11L, "dev", UserRole.EDITOR)))
            )
        )
    }
}