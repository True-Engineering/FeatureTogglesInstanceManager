package ru.trueengineering.featureflag.manager.core.domen.user

interface CreateUserUseCase {

    fun execute(command: CreateUserCommand): User

}

data class CreateUserCommand(
        val name: String,
        val email: String,
        var status: UserStatus? = null,
        var authorities: List<String>? = ArrayList()
)