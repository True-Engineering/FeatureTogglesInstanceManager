package ru.trueengineering.featureflag.manager.ports.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.core.domen.project.EnvironmentRole
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectUser
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.domen.user.UserRole
import ru.trueengineering.featureflag.manager.core.domen.user.UserStatus
import ru.trueengineering.featureflag.manager.ports.rest.controller.EnvironmentRoleDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.ProjectUserDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.UserDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.UserSettings

class ProjectUserMapperTest(
    @Autowired override var uut: ProjectUserMapper
) : MapperSpec<ProjectUserMapper, ProjectUser, ProjectUserDto> {


    override fun verifyDto(actualDto: ProjectUserDto) {
        assertThat(actualDto.projectRole).isEqualTo(CustomRole.MEMBER)
        assertThat(actualDto.projectId).isEqualTo(123L)
        assertThat(actualDto.projectName).isEqualTo("projectName")
        assertThat(actualDto.environmentPermissions).isEqualTo(listOf(EnvironmentRoleDto(11L, "dev", UserRole.VIEWER)))
        assertThat(actualDto.user).isEqualTo(
            UserDto(
                "name",
                "email",
                "https://www.gravatar.com/avatar/0c83f57c786a0b4a39efab23731c7ebc.jpg?d=404",
                listOf("READ"),
                11L,
                null,
                UserStatus.ACTIVE,
                UserSettings(23L)
            )
        )
    }

    override fun buildDomain(): ProjectUser {
        return ProjectUser(
            123L,
            "projectName",
            User("name", "email", 11L, null, UserStatus.ACTIVE, authorities = listOf("READ"), 23L),
            projectRole = CustomRole.MEMBER,
            environmentPermissions = listOf(EnvironmentRole(11L, "dev", UserRole.VIEWER))
        )
    }
}