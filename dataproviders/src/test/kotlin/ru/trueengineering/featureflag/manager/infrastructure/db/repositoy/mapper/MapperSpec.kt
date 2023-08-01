package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mapstruct.factory.Mappers
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [MapperSpec.MapperTestConfiguration::class])
interface MapperSpec<T : BaseEntityMapper<D, E>, E, D> {

    var uut: T

    @Test
    fun `should convert to entity`() = verifyEntity(uut.convertToEntity(buildDomain()))

    @Test
    fun `should convert to domain`() = verifyDomain(uut.convertToDomain(buildEntity()))

    @Test
    fun `should convert to entity list`() {
        val actualList = uut.convertToEntityList(listOf(buildDomain()))
        Assertions.assertEquals(1, actualList.size)
        verifyEntity(actualList.get(0))
    }

    @Test
    fun `should convert to domain list`() {
        val actualList = uut.convertToDomainList(listOf(buildEntity()))
        Assertions.assertEquals(1, actualList.size)
        verifyDomain(actualList.get(0))
    }

    fun verifyEntity(actualEntity: D)

    fun verifyDomain(actual: E)

    fun buildDomain(): E

    fun buildEntity(): D

    class MapperTestConfiguration {

        @Bean
        fun environmentMapper() = Mappers.getMapper(EnvironmentEntityMapper::class.java)

        @Bean
        fun organizationMapper() = Mappers.getMapper(OrganizationEntityMapper::class.java)

        @Bean
        fun projectMapper() = Mappers.getMapper(ProjectEntityMapper::class.java)


        @Bean
        fun featureFlagMapper() = Mappers.getMapper(FeatureFlagEntityMapper::class.java)

        @Bean
        fun userMapper() = Mappers.getMapper(UserEntityMapper::class.java)

        @Bean
        fun featureFlagEnvironmentEntityMapper(): FeatureFlagEnvironmentEntityMapper? {
            val mapper = Mappers.getMapper(FeatureFlagEnvironmentEntityMapper::class.java)
            mapper.objectMapper = ObjectMapper()
            return mapper
        }

        @Bean
        fun invitationEntityMapper() = Mappers.getMapper(InvitationEntityMapper::class.java)

        @Bean
        fun featureChangesMapper() = Mappers.getMapper(FeatureChangesEntityMapper::class.java)

        @Bean
        fun changesHistoryMapper() = Mappers.getMapper(ChangesHistoryEntityMapper::class.java)
    }
}