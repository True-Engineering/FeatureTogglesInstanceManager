package ru.trueengineering.featureflag.manager.core.domen.event

interface DomainEventHandler<T: DomainEvent> {
    fun handle(event: T)
}