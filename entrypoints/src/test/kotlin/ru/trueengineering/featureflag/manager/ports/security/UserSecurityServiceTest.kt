package ru.trueengineering.featureflag.manager.ports.security

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import ru.trueengineering.featureflag.manager.core.domen.user.CreateUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUserByEmailQuery
import ru.trueengineering.featureflag.manager.core.domen.user.FetchUserUseCase
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.ports.security.user.UserSecurityService

internal class UserSecurityServiceTest {

    private val fetchUserUseCase: FetchUserUseCase = mockk()
    private val createUserUseCase: CreateUserUseCase = mockk()

    private val uut: UserSecurityService = UserSecurityService(fetchUserUseCase, createUserUseCase)

    @Test
    fun authenticateExistedUser() {
        val context: SecurityContext = mockk()
        val slot = slot<Authentication>()
        val user = User("name", "email")
        SecurityContextHolder.setContext(context)
        every { context.authentication = capture(slot) } just Runs
        every { createUserUseCase.execute(any()) } returns user
        every { fetchUserUseCase.execute(eq(FetchUserByEmailQuery("email"))) } returns user
        uut.authenticate(user)

        val capture = slot.captured
        assertEquals(user, capture.principal)
        verify(exactly = 0) { createUserUseCase.execute(any()) }
    }

    @Test
    fun authenticateNewUser() {
        val context: SecurityContext = mockk()
        val slot = slot<Authentication>()
        val user = User("name", "email")
        SecurityContextHolder.setContext(context)
        every { context.authentication = capture(slot) } just Runs
        every { createUserUseCase.execute(any()) } returns user
        every { fetchUserUseCase.execute(eq(FetchUserByEmailQuery("email"))) } returns null
        uut.authenticate(user)

        val capture = slot.captured
        assertEquals(user, capture.principal)
        verify { createUserUseCase.execute(any()) }
    }

}