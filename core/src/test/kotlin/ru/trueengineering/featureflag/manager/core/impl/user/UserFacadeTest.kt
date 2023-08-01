package ru.trueengineering.featureflag.manager.core.impl.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import ru.trueengineering.featureflag.manager.core.domen.user.CreateUserCommand
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUserByEmailQuery
import ru.trueengineering.featureflag.manager.core.domen.user.SetDefaultProjectCommand
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.domen.user.UserStatus
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class UserFacadeTest {

    private val userRepository: UserRepository = mockk()

    val uut: UserFacade = UserFacade(userRepository)

    val instant: Instant = Instant.now()

    @Test
    fun search() {
        val context: SecurityContext = mockk()
        val authentication: Authentication = mockk()
        SecurityContextHolder.setContext(context)
        every { context.authentication } returns authentication
        val expected = User(userName = "Test Test", email = "email", authorities =  emptyList())
        every { authentication.principal } returns expected
        every { authentication.authorities } returns listOf(SimpleGrantedAuthority("ROLE_USER"))

        val actual = uut.search()

        assertThat(actual).isNotNull.isEqualTo(expected)
        assertThat(actual.authorities).isEqualTo(listOf("ROLE_USER"))
        assertThat(actual.avatarUrl)
            .isNotNull
            .isEqualTo("https://www.gravatar.com/avatar/0c83f57c786a0b4a39efab23731c7ebc.jpg?d=404")

    }

    @Test
    fun fetchByEmail() {
        val expected = User("name", "email")
        every { userRepository.getByEmail("email") } returns expected
        val actual = uut.execute(FetchUserByEmailQuery("email"))
        assertEquals(expected, actual)
    }

    @Test
    fun create() {
        val slot = slot<User>()
        val expected = User("name", "email")
        every { userRepository.createUser(capture(slot)) } returns expected
        val actual =
            uut.execute(CreateUserCommand("name", "email", UserStatus.ACTIVE, emptyList()))
        assertEquals(expected, actual)
        val captured = slot.captured
        assertEquals("name", captured.userName)
        assertEquals("email", captured.email)
        assertNull(captured.id)
        assertNull(captured.lastLogin)
        assertEquals(UserStatus.ACTIVE, captured.status)
        assertEquals(emptyList(), captured.authorities)
    }

    @Test
    fun setDefaultProject() {
        val slot = slot<User>()
        val expected = User(userName = "Test Test", email = "email", authorities =  emptyList())
        every { userRepository.updateUser(capture(slot)) } returns expected

        val context: SecurityContext = mockk()
        val authentication: Authentication = mockk()
        SecurityContextHolder.setContext(context)
        every { context.authentication } returns authentication
        every { authentication.principal } returns expected

        uut.execute(SetDefaultProjectCommand(1L, true))

        val capture = slot.captured
        assertEquals(1, capture.defaultProjectId)
    }

    @Test
    fun deleteDefaultProject() {
        val slot = slot<User>()
        val expected = User(userName = "Test Test", email = "email", authorities =  emptyList())
        every { userRepository.updateUser(capture(slot)) } returns expected

        val context: SecurityContext = mockk()
        val authentication: Authentication = mockk()
        SecurityContextHolder.setContext(context)
        every { context.authentication } returns authentication
        every { authentication.principal } returns expected

        uut.execute(SetDefaultProjectCommand(1L, false))

        val capture = slot.captured
        assertEquals(null, capture.defaultProjectId)
    }
}