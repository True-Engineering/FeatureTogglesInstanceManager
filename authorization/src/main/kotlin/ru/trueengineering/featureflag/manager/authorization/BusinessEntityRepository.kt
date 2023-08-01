package ru.trueengineering.featureflag.manager.authorization

import ru.trueengineering.featureflag.manager.auth.BusinessEntity

interface BusinessEntityRepository {

    /**
     * Создание object identity для указанного бизнес сущности
     * @param entity        новая бизнес сущность
     */
    fun createBusinessEntity(entity: BusinessEntity);

    /**
     * Создание object identity для указанного бизнес объекта и привязывание его к родителю
     * @param entity        новая бизнес сущность
     * @param parent        родитель, должен существовать
     */
    fun createBusinessEntity(entity: BusinessEntity, parent: BusinessEntity);
}