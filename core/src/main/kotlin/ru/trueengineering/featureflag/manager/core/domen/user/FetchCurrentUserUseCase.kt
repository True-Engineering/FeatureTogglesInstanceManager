package ru.trueengineering.featureflag.manager.core.domen.user

/**
 * Возвращает текущего юзера из SecurityContext
 */
interface FetchCurrentUserUseCase {

    fun search() : User
}
