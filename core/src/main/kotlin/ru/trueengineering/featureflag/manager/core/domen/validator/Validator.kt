package ru.trueengineering.featureflag.manager.core.domen.validator

import ru.trueengineering.featureflag.manager.core.error.ErrorCode


interface Validator<T> {

    fun validate(obj: T): ValidatorResponse

}

data class ValidatorResponse(
    val isValid: Boolean,
    val errorCode: ErrorCode? = null,
    val message: String? = null
)