package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.transaction.TestTransaction
import ru.trueengineering.featureflag.manager.core.domen.event.DomainEvent
import ru.trueengineering.featureflag.manager.core.domen.event.UserCreatedEvent
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.JpaRepositoryBaseTest
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.capture

internal class UserDatabaseRepositoryIT(@Autowired override var uut: UserDatabaseRepository) :
    JpaRepositoryBaseTest<UserDatabaseRepository>() {

    @MockBean
    private lateinit var eventHandler: TestEventHandler
    private val captor: ArgumentCaptor<DomainEvent> = ArgumentCaptor.forClass(DomainEvent::class.java)

    @Test
    internal fun `should create new user and produce domain event`() {
        val actualUser = uut.createUser(User(email = "newEmail@test.ru", userName = "Pavel"))
        TestTransaction.flagForCommit()
        TestTransaction.end()

        assertThat(actualUser.id).isNotNull
        assertThat(actualUser.email).isEqualTo("newEmail@test.ru")
        assertThat(actualUser.userName).isEqualTo("Pavel")

        Mockito.verify(eventHandler).handle(capture(captor))
        val domainEvent = captor.value
        assertThat(domainEvent).isNotNull
        assertThat(domainEvent).isInstanceOf(UserCreatedEvent::class.java)
        domainEvent as UserCreatedEvent
        assertThat(domainEvent.userEmail).isEqualTo("newEmail@test.ru")
    }
}