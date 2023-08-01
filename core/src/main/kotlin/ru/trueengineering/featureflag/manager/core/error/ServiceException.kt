package ru.trueengineering.featureflag.manager.core.error

class ServiceException(val errorCode: ErrorCode, cause: Throwable? = null) : Exception(cause) {

    var errorMessage: String? = null

    constructor(errorCode: ErrorCode, message: String) : this(errorCode) {
        this.errorMessage = message
    }

    override fun toString(): String {
        return "ServiceException(errorCode=$errorCode, errorMessage=$errorMessage)"
    }

}