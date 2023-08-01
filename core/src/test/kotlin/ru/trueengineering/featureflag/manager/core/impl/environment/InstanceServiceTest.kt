package ru.trueengineering.featureflag.manager.core.impl.environment

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

private const val PERIOD = 100

internal class InstanceServiceTest {

    private val environmentRepository: EnvironmentRepository = mockk()

    val uut: InstanceService = InstanceService(environmentRepository, PERIOD)

    @Test
    fun updateInstanceStatus() {
        every { environmentRepository.checkAndUpdateInstanceStatus(100) } just Runs
        uut.updateInstanceStatus()
        verify { environmentRepository.checkAndUpdateInstanceStatus(100) }
    }
}