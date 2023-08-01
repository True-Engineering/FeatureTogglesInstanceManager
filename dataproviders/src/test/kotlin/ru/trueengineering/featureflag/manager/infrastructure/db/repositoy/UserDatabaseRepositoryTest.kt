package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.UserEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.UserJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.UserEntityMapper
import kotlin.test.assertEquals

internal class UserDatabaseRepositoryTest {

    private val userJpaRepository: UserJpaRepository = mockk()
    private val userEntityMapper: UserEntityMapper = mockk()

    private val uut = UserDatabaseRepository(userJpaRepository, userEntityMapper)

    @Test
    fun getById() {

        val userEntity = UserEntity("email")

        val expected = User("name", "email")

        every { userJpaRepository.getNonRemovedById(1) } returns userEntity
        every { userEntityMapper.convertToDomain(userEntity) } returns expected


        val actual = uut.getById(1L)
        assertEquals(expected, actual)

    }
}