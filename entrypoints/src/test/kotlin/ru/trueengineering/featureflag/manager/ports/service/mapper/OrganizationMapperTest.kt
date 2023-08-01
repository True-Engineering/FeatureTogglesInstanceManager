package ru.trueengineering.featureflag.manager.ports.service.mapper

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.environment.Instance
import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.organization.Organization
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.ports.rest.controller.EnvironmentInfoDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.InstanceDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.OrganizationDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.ProjectDto
import java.time.Instant
import kotlin.test.assertEquals

class OrganizationMapperTest(
    @Autowired override var uut: OrganizationMapper
) : MapperSpec<OrganizationMapper, Organization, OrganizationDto> {

    private val instant: Instant = Instant.now()

    override fun verifyDto(actualDto: OrganizationDto) {
        val instanceDto = InstanceDto(1, "instance", instant, InstanceConnectionStatus.OUT_OF_SYNC)
        val environmentInfoDto =
            EnvironmentInfoDto(2, "envName", listOf(instanceDto), false, EnvironmentConnectionStatus.OUT_OF_SYNC,
                actualDto.projects?.get(0)?.environments?.get(0)?.permissions, listOf()
            )
        val projectDto = ProjectDto(
            id = 2L,
            name = "prj",
            environments = listOf(environmentInfoDto),
            //todo пока не сделали правильное заполнение permissions
            permissions = actualDto.projects?.get(0)?.permissions,
            status = EnvironmentConnectionStatus.OUT_OF_SYNC,
        )
        assertEquals(1, actualDto.id)
        assertEquals("orgName", actualDto.name)
        assertEquals(listOf(projectDto), actualDto.projects)
        Assertions.assertEquals(actualDto.permissions, mutableSetOf("READ", "EDIT"))
    }

    override fun buildDomain(): Organization {
        return Organization(
            id = 1,
            name = "orgName",
            projects = listOf(
                Project(
                    2,
                    "prj",
                    mutableListOf(
                        Environment(
                            2,
                            "envName",
                            null,
                            instances = listOf(
                                Instance(
                                    1,
                                    "instance",
                                    instant,
                                    InstanceConnectionStatus.OUT_OF_SYNC
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}