package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import kotlin.test.assertNotNull


abstract class JpaRepositoryBaseTest<T> : JpaBaseTest() {

    abstract var uut: T

    @Test
    internal fun shouldStartUp() {
        assertNotNull(uut)
        assertNotNull(postgres)
    }

}

/**
 * Matcher that returns null
 */
inline fun <reified T> any(): T = Mockito.any<T>()

fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

/**
 * Matcher never returns null
 */
inline fun <reified T> any(type: Class<T>): T = Mockito.any(type)