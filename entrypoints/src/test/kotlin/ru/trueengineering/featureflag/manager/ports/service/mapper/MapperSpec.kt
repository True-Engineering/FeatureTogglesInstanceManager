package ru.trueengineering.featureflag.manager.ports.service.mapper

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mapstruct.factory.Mappers
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.trueengineering.featureflag.manager.auth.IPermissionService

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = arrayOf(MapperSpec.MapperTestConfiguration::class))
interface MapperSpec<T : BaseMapper<E, D>, E, D> {

    var uut: T

    @Test
    fun `should convert to dto`() = verifyDto(uut.convertToDto(buildDomain()))

    @Test
    fun `should convert to dto list`() {
        val actualDtoList = uut.convertToDtoList(listOf(buildDomain()))
        Assertions.assertEquals(1, actualDtoList.size)
        verifyDto(actualDtoList.get(0))
    }

    fun verifyDto(actualDto: D)

    fun buildDomain(): E

    class MapperTestConfiguration {


        @Bean
        fun environmentMapper() = Mappers.getMapper(EnvironmentMapper::class.java)

        @Bean
        fun organizationMapper() = Mappers.getMapper(OrganizationMapper::class.java)

        @Bean
        fun projectMapper() = Mappers.getMapper(ProjectMapper::class.java)

        @Bean
        fun userMapper() = Mappers.getMapper(UserMapper::class.java)


        @Bean
        fun featureFlagMapperMapper() = Mappers.getMapper(FeatureFlagMapper::class.java)

        @Bean
        fun organizationUserMapper() = Mappers.getMapper(OrganizationUserMapper::class.java)

        @Bean
        fun projectUserMapper() = Mappers.getMapper(ProjectUserMapper::class.java)

        @Bean
        fun permissionService(): IPermissionService {
            val mockk = mockk<IPermissionService>()
            every { mockk.getPermissionsNameListForCurrentUser(any()) } returns mutableSetOf("READ", "EDIT")
            return mockk
        }
    }
}