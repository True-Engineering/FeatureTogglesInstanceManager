package ru.trueengineering.featureflag.manager.core.domen.environment

enum class InstanceConnectionStatus {
    /**
     * соединение установлено, состояние фича флагов синхронизировано
     */
    ACTIVE,

    /**
     * соединение установлено, состояние фиса флагов рассинхронизировано
     */
    OUT_OF_SYNC,

    /**
     * Соединение потеряно
     */
    UNAVAILABLE
}