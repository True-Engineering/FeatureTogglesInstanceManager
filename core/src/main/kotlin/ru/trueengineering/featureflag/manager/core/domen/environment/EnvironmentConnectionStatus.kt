package ru.trueengineering.featureflag.manager.core.domen.environment

enum class EnvironmentConnectionStatus {
    /**
     * Окружение подключено
     */
    ACTIVE,

    /**
     * Окружение подключено, но его состояние устарело
     */
    OUT_OF_SYNC,

    /**
     * Окружение недоступно
     */
    UNAVAILABLE,

    /**
     * Окружение неподключено
     */
    NOT_CONNECTED
}