package ru.trueengineering.featureflag.manager.core.impl.user

import ru.trueengineering.featureflag.manager.core.domen.user.User

interface UserRepository {

    /**
     *  Создание нового пользователя в системе
     */
    fun createUser(user: User) : User

    /**
     * Обновление существующего пользователя
     */
    fun updateUser(user: User) : User

    fun getById(userId: Long) : User?

    fun getByEmail(email: String) : User?

    fun getByEmailList(emails: List<String>) : List<User>

    fun getCountByEmailList(emails: List<String>) : Int
}