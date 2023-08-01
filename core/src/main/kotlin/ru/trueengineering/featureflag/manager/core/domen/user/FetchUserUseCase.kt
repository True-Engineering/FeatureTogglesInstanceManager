package ru.trueengineering.featureflag.manager.core.domen.user

/**
 * Возвращает юзера из БД
 */
interface FetchUserUseCase {

    fun execute(command: FetchUserByEmailQuery) : User?

    fun execute(command: FetchUsersByEmailListQuery) : List<User>

    fun searchUserCount(command: FetchUsersByEmailListQuery) : Int

    fun searchById(userId: Long) : User?
}

data class FetchUserByEmailQuery(val email: String)
data class FetchUsersByEmailListQuery(val emails: List<String>)
