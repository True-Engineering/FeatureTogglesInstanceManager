package ru.trueengineering.featureflag.manager.core.error

enum class ErrorCode {
    ORGANIZATION_NOT_FOUND,
    ENVIRONMENT_NOT_FOUND,
    PROJECT_NOT_FOUND,
    UNABLE_TO_SAVE_PROJECT,
    PROJECT_ALREADY_EXIST,
    ENVIRONMENT_ALREADY_EXIST,
    UNABLE_TO_SAVE_ENVIRONMENT,
    UNABLE_TO_SAVE_FEATURE_FLAG,
    ACCESS_DENIED,
    INVALID_INPUT_DATA,
    INTERNAL_ERROR,
    FEATURE_FLAG_NOT_FOUND,
    ORGANIZATION_ALREADY_EXIST,
    USER_NOT_FOUND,
    USER_IS_NOT_INVITED_TO_PROJECT,
    INVITATION_NOT_FOUND,
    UNABLE_TO_READ_FILE,
    UNABLE_TO_FREEZE_ENVIRONMENT,
    ENVIRONMENT_IS_FROZEN
}
