package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.domen.user.UserStatus
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.UserEntity
import java.time.Instant

class UserEntityMapperTest(
    @Autowired override var uut: UserEntityMapper
) : MapperSpec<UserEntityMapper, User, UserEntity> {

    private val instant: Instant = Instant.now()

    @Test
    internal fun `should update entity`() {
        val entity = buildEntity()
        uut.updateEntity(
            entity,
            User(
                userName = "newName",
                email = "some new email",
                1L,
                null,
                status = UserStatus.BLOCKED,
                authorities = listOf("auth"),
                12L
            )
        )

        assertThat(entity.id).isEqualTo(1)
        assertThat(entity.name).isEqualTo("newName")
        assertThat(entity.email).isEqualTo("some new email")
        assertThat(entity.status).isEqualTo(UserStatus.BLOCKED)
        assertThat(entity.defaultProjectId).isEqualTo(12)
    }

    override fun verifyEntity(actualEntity: UserEntity) {
        val expected = buildEntity()
        assertThat(actualEntity)
            .usingRecursiveComparison()
            .isEqualTo(expected)

    }

    override fun buildDomain(): User {
        return User(userName = "name", email = "email", id = 1L, lastLogin = instant, status = UserStatus.ACTIVE)
    }

    override fun verifyDomain(actual: User) {
        val expected = buildDomain()
        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected)

    }

    override fun buildEntity(): UserEntity {
        return UserEntity(email = "email").apply {
            name = "name"
            id = 1L
            lastLogin = instant
            status = UserStatus.ACTIVE
        }
    }
}