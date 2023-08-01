package ru.trueengineering.featureflag.manager.ports.service.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.environment.Instance
import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
import ru.trueengineering.featureflag.manager.ports.rest.controller.EnvironmentInfoDto
import java.time.Instant
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class EnvironmentMapperTest(
    @Autowired override var uut: EnvironmentMapper
) : MapperSpec<EnvironmentMapper, Environment, EnvironmentInfoDto> {

    override fun buildDomain(): Environment {
        val instance = Instance(1, "instanceName", Instant.now(), InstanceConnectionStatus.ACTIVE)
        return Environment(
            1,
            "envName",
            "authKeyHash",
            listOf(instance)
        )
    }

    override fun verifyDto(actualDto: EnvironmentInfoDto) {
        assertEquals(1, actualDto.id)
        assertEquals("envName", actualDto.name)
        assertEquals(1, actualDto.instances.size)
        assertEquals("instanceName", actualDto.instances.get(0).name)
        assertTrue(actualDto.authKeyExist)
        assertEquals(InstanceConnectionStatus.ACTIVE, actualDto.instances.get(0).status)
        assertEquals(actualDto.permissions, mutableSetOf("READ", "EDIT"))
    }

    @Test
    internal fun evaluateEnvStatusTest1() {
        val environmentInfoDto = uut.convertToDto(
            buildEnvironmentInfoDto(
                listOf(
                    InstanceConnectionStatus.ACTIVE,
                    InstanceConnectionStatus.ACTIVE,
                    InstanceConnectionStatus.ACTIVE
                )
            )
        )
        assertEquals(EnvironmentConnectionStatus.ACTIVE, environmentInfoDto.status)
    }

    @ParameterizedTest
    @MethodSource("provideStatuses")
    internal fun evaluateEnvStatusTest(
        statuses: List<InstanceConnectionStatus>,
        expectedStatus: EnvironmentConnectionStatus
    ) {
        val environmentInfoDto = uut.convertToDto(buildEnvironmentInfoDto(statuses))
        assertEquals(expectedStatus, environmentInfoDto.status)
    }

    private fun buildEnvironmentInfoDto(statuses: List<InstanceConnectionStatus>): Environment {
        val instances = ArrayList<Instance>()
        var index = 0L
        statuses.forEach { instances.add(Instance(++index, "Agent $index", Instant.now(), it)) }
        return Environment(1, "PROD", "authTokenHash", instances)
    }

    fun provideStatuses(): Stream<Arguments?>? {
        return Stream.of(
            Arguments.of(
                emptyList<InstanceConnectionStatus>(), EnvironmentConnectionStatus.NOT_CONNECTED
            ),
            Arguments.of(
                listOf(
                    InstanceConnectionStatus.ACTIVE,
                    InstanceConnectionStatus.ACTIVE,
                    InstanceConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.ACTIVE
            ),
            Arguments.of(
                listOf(
                    InstanceConnectionStatus.ACTIVE,
                    InstanceConnectionStatus.OUT_OF_SYNC,
                    InstanceConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.OUT_OF_SYNC
            ),
            Arguments.of(
                listOf(
                    InstanceConnectionStatus.OUT_OF_SYNC,
                    InstanceConnectionStatus.ACTIVE,
                    InstanceConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.OUT_OF_SYNC
            ),
            Arguments.of(
                listOf(
                    InstanceConnectionStatus.UNAVAILABLE,
                    InstanceConnectionStatus.ACTIVE,
                    InstanceConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.UNAVAILABLE
            ),
            Arguments.of(
                listOf(
                    InstanceConnectionStatus.ACTIVE,
                    InstanceConnectionStatus.UNAVAILABLE,
                    InstanceConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.UNAVAILABLE
            ),
            Arguments.of(
                listOf(
                    InstanceConnectionStatus.ACTIVE,
                    InstanceConnectionStatus.OUT_OF_SYNC,
                    InstanceConnectionStatus.UNAVAILABLE
                ), EnvironmentConnectionStatus.UNAVAILABLE
            ),
            Arguments.of(
                listOf(
                    InstanceConnectionStatus.ACTIVE,
                    InstanceConnectionStatus.UNAVAILABLE,
                    InstanceConnectionStatus.OUT_OF_SYNC
                ), EnvironmentConnectionStatus.UNAVAILABLE
            ),
            Arguments.of(
                listOf(
                    InstanceConnectionStatus.UNAVAILABLE,
                    InstanceConnectionStatus.ACTIVE,
                    InstanceConnectionStatus.OUT_OF_SYNC
                ), EnvironmentConnectionStatus.UNAVAILABLE
            ),
            Arguments.of(
                listOf(
                    InstanceConnectionStatus.UNAVAILABLE,
                    InstanceConnectionStatus.OUT_OF_SYNC,
                    InstanceConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.UNAVAILABLE
            ),
            Arguments.of(
                listOf(
                    InstanceConnectionStatus.OUT_OF_SYNC,
                    InstanceConnectionStatus.ACTIVE,
                    InstanceConnectionStatus.UNAVAILABLE
                ), EnvironmentConnectionStatus.UNAVAILABLE
            ),
            Arguments.of(
                listOf(
                    InstanceConnectionStatus.OUT_OF_SYNC,
                    InstanceConnectionStatus.UNAVAILABLE,
                    InstanceConnectionStatus.ACTIVE
                ), EnvironmentConnectionStatus.UNAVAILABLE
            )
        )
    }
}