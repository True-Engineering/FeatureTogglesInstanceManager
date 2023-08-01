package ru.trueengineering.featureflag.manager.ports.service.mapper

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.environment.Instance
import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.ports.rest.controller.EnvironmentInfoDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.InstanceDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.ProjectDto
import java.time.Instant
import java.util.stream.Stream
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectMapperTest(@Autowired override var uut: ProjectMapper) : MapperSpec<ProjectMapper, Project, ProjectDto> {

    private val instant: Instant = Instant.now()

    override fun verifyDto(actualDto: ProjectDto) {
        val instanceDto = InstanceDto(1, "instance", instant, InstanceConnectionStatus.OUT_OF_SYNC)
        //todo пока не сделали правильное заполнение permissions
        val environmentInfoDto =
            EnvironmentInfoDto(2, "envName", listOf(instanceDto), true, EnvironmentConnectionStatus.OUT_OF_SYNC, actualDto.environments[0].permissions, listOf())
        assertEquals(2, actualDto.id)
        assertEquals("prj", actualDto.name)
        assertEquals(listOf(environmentInfoDto), actualDto.environments)
        Assertions.assertEquals(actualDto.permissions, mutableSetOf("READ", "EDIT"))
    }

    override fun buildDomain(): Project {
        return Project(
            2,
            "prj",
            mutableListOf(
                Environment(
                    2,
                    "envName",
                    "Auth Key Hash",
                    instances = listOf(
                        Instance(
                            1,
                            "instance",
                            instant,
                            InstanceConnectionStatus.OUT_OF_SYNC
                        )
                    ),
                )
            )
        )
    }

    @ParameterizedTest
    @MethodSource("provideStatuses")
    internal fun evaluateEnvStatusTest(
        statuses: List<EnvironmentConnectionStatus>,
        expectedStatus: EnvironmentConnectionStatus
    ) {
        val project = buildProjectInfoDto(statuses)
        uut.evaluateStatus(project)
        assertEquals(expectedStatus, project.status)
    }

    private fun buildProjectInfoDto(statuses: List<EnvironmentConnectionStatus>): ProjectDto {
        val environments = ArrayList<EnvironmentInfoDto>()
        var index = 0L
        statuses.forEach { environments.add(EnvironmentInfoDto(id = ++index, name = "Agent $index", status = it, authKeyExist = true, emails = listOf(), instances = emptyList())) }
        return ProjectDto(1, "Project", environments)
    }

    private fun provideStatuses(): Stream<Arguments?>? {
        return Stream.of(
            Arguments.of(
                emptyList<EnvironmentConnectionStatus>(), EnvironmentConnectionStatus.NOT_CONNECTED
            ),
            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.NOT_CONNECTED,
                    EnvironmentConnectionStatus.ACTIVE,
                    EnvironmentConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.NOT_CONNECTED
            ),
            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.NOT_CONNECTED,
                    EnvironmentConnectionStatus.OUT_OF_SYNC,
                    EnvironmentConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.NOT_CONNECTED
            ),

            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.ACTIVE,
                    EnvironmentConnectionStatus.OUT_OF_SYNC,
                    EnvironmentConnectionStatus.NOT_CONNECTED
                ), EnvironmentConnectionStatus.NOT_CONNECTED
            ),
            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.ACTIVE,
                    EnvironmentConnectionStatus.UNAVAILABLE,
                    EnvironmentConnectionStatus.NOT_CONNECTED
                ), EnvironmentConnectionStatus.NOT_CONNECTED
            ),

            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.OUT_OF_SYNC,
                    EnvironmentConnectionStatus.UNAVAILABLE,
                    EnvironmentConnectionStatus.NOT_CONNECTED
                ), EnvironmentConnectionStatus.NOT_CONNECTED
            ),

            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.ACTIVE,
                    EnvironmentConnectionStatus.ACTIVE,
                    EnvironmentConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.ACTIVE
            ),
            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.ACTIVE,
                    EnvironmentConnectionStatus.OUT_OF_SYNC,
                    EnvironmentConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.OUT_OF_SYNC
            ),
            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.OUT_OF_SYNC,
                    EnvironmentConnectionStatus.ACTIVE,
                    EnvironmentConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.OUT_OF_SYNC
            ),
            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.UNAVAILABLE,
                    EnvironmentConnectionStatus.ACTIVE,
                    EnvironmentConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.UNAVAILABLE
            ),
            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.ACTIVE,
                    EnvironmentConnectionStatus.UNAVAILABLE,
                    EnvironmentConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.UNAVAILABLE
            ),
            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.ACTIVE,
                    EnvironmentConnectionStatus.OUT_OF_SYNC,
                    EnvironmentConnectionStatus.UNAVAILABLE
                ), EnvironmentConnectionStatus.UNAVAILABLE
            ),
            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.ACTIVE,
                    EnvironmentConnectionStatus.UNAVAILABLE,
                    EnvironmentConnectionStatus.OUT_OF_SYNC
                ), EnvironmentConnectionStatus.UNAVAILABLE
            ),
            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.UNAVAILABLE,
                    EnvironmentConnectionStatus.ACTIVE,
                    EnvironmentConnectionStatus.OUT_OF_SYNC
                ), EnvironmentConnectionStatus.UNAVAILABLE
            ),
            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.UNAVAILABLE,
                    EnvironmentConnectionStatus.OUT_OF_SYNC,
                    EnvironmentConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.UNAVAILABLE
            ),
            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.OUT_OF_SYNC,
                    EnvironmentConnectionStatus.ACTIVE,
                    EnvironmentConnectionStatus.UNAVAILABLE
                ), EnvironmentConnectionStatus.UNAVAILABLE
            ),
            Arguments.of(
                listOf(
                    EnvironmentConnectionStatus.OUT_OF_SYNC,
                    EnvironmentConnectionStatus.UNAVAILABLE,
                    EnvironmentConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.UNAVAILABLE
            )
        )
    }
}