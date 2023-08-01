package ru.trueengineering.featureflag.manager.core.impl.validator

import org.springframework.stereotype.Component
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.toggle.CreateFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.toggle.EditFeatureFlagCommand
import ru.trueengineering.featureflag.manager.core.domen.validator.Validator
import ru.trueengineering.featureflag.manager.core.error.ServiceException

sealed interface ValidatorHandler<T> {

    val validators: List<Validator<T>>

    fun validateOrThrow(obj: T): T {
        val invalid = validators.map { it.validate(obj) }.filter { !it.isValid }

        if (invalid.isNotEmpty()) {
            throw ServiceException(
                invalid.first().errorCode!!,
                invalid.first().message!!
            )
        }

        return obj
    }
}

@Component
class ProjectValidatorHandler(
    override val validators: List<Validator<Project>>
): ValidatorHandler<Project>

@Component
class CreateFeatureFlagCommandValidatorHandler(
    override val validators: List<Validator<CreateFeatureFlagCommand>>
): ValidatorHandler<CreateFeatureFlagCommand>

@Component
class EditFeatureFlagCommandValidatorHandler(
    override val validators: List<Validator<EditFeatureFlagCommand>>
): ValidatorHandler<EditFeatureFlagCommand>