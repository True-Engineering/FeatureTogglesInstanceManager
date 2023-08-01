package ru.trueengineering.featureflag.manager.core.domen.user

interface SetDefaultProjectUseCase {

    fun execute(command: SetDefaultProjectCommand)

}

data class SetDefaultProjectCommand(val projectId: Long, val isDefault: Boolean)