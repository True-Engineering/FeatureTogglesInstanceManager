package ru.trueengineering.featureflag.manager.authorization.config;

import org.springframework.security.acls.domain.AbstractPermission;
import org.springframework.security.acls.model.Permission;

public class CustomPermission extends AbstractPermission {

    /**
     * Право на чтение конкретной сущности.
     * Например, hasPermission(project, 'READ_PROJECT')
     * проверяет право на чтение на чтение указанного проекта
     * Право распространяется на вложенные сущности,
     * т.е, если у пользователя есть READ_PROJECT право на организацию,
     * то у него есть права на чтение всех проектов этой организации, но нет прав на чтение других сущностей,
     * например, окружений
     */
    public static final CustomPermission READ_PROJECT = new CustomPermission(1 << 0, 'R'); // 1

    /**
     * Право на чтение организации.
     * Например, hasPermission(organization, 'READ_ORGANIZATION')
     * проверяет право на чтение указанной организации.
     * Право НЕ распространяется на вложенные сущности,
     * т.е, пользователь может видеть только те проекты, на которые у него есть отдельное право READ_PROJECT
     */
    public static final CustomPermission READ_ORGANIZATION = new CustomPermission(1 << 1, 'O'); // 2

    /**
     * Право на чтение списка пользователей указанной сущности.
     * Например, hasPermission(organization, 'READ_MEMBERS')
     * проверяет право на чтение пользователей указанной организации.
     * Право распространяется на все вложенные сущности,
     * т.е, если у пользователя есть READ_MEMBERS право на организацию,
     * то у него есть READ_MEMBERS права на все проекты этой организации
     */
    public static final CustomPermission READ_MEMBERS = new CustomPermission(1 << 2, 'M'); // 4

    /**
     * Право на редактирование списка пользователей указанной сущности.
     * Например, hasPermission(organization, 'EDIT_MEMBERS')
     * проверяет право на редактирование пользователей указанной организации.
     * Право распространяется на все вложенные сущности,
     * т.е, если у пользователя есть EDIT_MEMBERS право на организацию,
     * то у него есть CREATE_MEMBERS права на все проекты этой организации
     * и все окружения и фичефлаги этих проектов
     */
    public static final CustomPermission EDIT_MEMBERS = new CustomPermission(1 << 3, 'E'); // 8

    /**
     * Право на создание проектов.
     * Например, hasPermission(organization, 'CREATE_PROJECT')
     * проверяет право на создание проектов в указанной организации.
     * Необходимо применять только для проверки прав на сущность OrganizationEntity
     */
    public static final CustomPermission CREATE_PROJECT = new CustomPermission(1 << 4, 'C'); // 16

    /**
     * Право на изменение конкретной сущности.
     * Например, hasPermission(organization, 'EDIT')
     * дает пользователю право на изменение указанной организации
     * Право распространяется на все вложенные сущности,
     * т.е, если у пользователя есть EDIT право на организацию,
     * то у него есть EDIT права на все проекты этой организации
     * и все окружения и фичефлаги этих проектов
     */
    public static final CustomPermission EDIT = new CustomPermission(1 << 5, 'W'); // 32

    /**
     * Право на создание окружений.
     * Например, hasPermission(project, 'CREATE_ENV')
     * проверяет право на создание окружений в указанном проекте.
     * Необходимо применять только для проверки прав на сущность ProjectEntity
     */
    public static final CustomPermission CREATE_ENV = new CustomPermission(1 << 6, 'N'); // 64

    /**
     * Право на создание фичефлагов.
     * Например, hasPermission(project, 'CREATE_FLAG')
     * проверяет право на создание фичефлагов в указанном проекте.
     * Необходимо применять только для проверки прав на сущность ProjectEntity
     */
    public static final CustomPermission CREATE_FLAG = new CustomPermission(1 << 7, 'F'); // 128

    /**
     * Право на удаление фичефлагов.
     * Например, hasPermission(project, 'DELETE_FLAG')
     * проверяет право на удаление фичефлагов в указанном проекте.
     * Необходимо применять только для проверки прав на сущность ProjectEntity
     */
    public static final CustomPermission DELETE_FLAG = new CustomPermission(1 << 8, 'K'); // 256

    /**
     * Право на удаление конкретной сущности.
     * Например, hasPermission(project, 'DELETE')
     * проверяет право на удаление указанного проекта
     * Право распространяется на все вложенные сущности,
     * т.е, если у пользователя есть DELETE право на организацию,
     * то у него есть DELETE права на все проекты этой организации
     * и все окружения и фичефлаги этих проектов
     */
    public static final CustomPermission DELETE = new CustomPermission(1 << 9, 'D'); // 512

    /**
     * Право на чтение конкретного environment.
     * Например, hasPermission(environment, 'READ_ENVIRONMENT')
     * проверяет право на чтение указанного окружения.
     * Право распространяется на все вложенные сущности,
     * т.е, если у пользователя есть READ_ENVIRONMENT право на проект или организацию,
     * то у него есть READ_ENVIRONMENT права на все окружения этого проекта или организации
     */
    public static final CustomPermission READ_ENVIRONMENT = new CustomPermission(1 << 10, 'R'); // 1024

    /**
     * Право, которое получает пользователь после приглашения в проект
     * Например, hasPermission(environment, 'PENDING_APPROVE')
     * После получения данного права пользователь попадает в список пользователей для подтверждения участия в проекте.
     * После подтверждения со стороны админа проекта (имеющего право EDIT_MEMBERS) это право удаляется
     * и назначается право READ_PROJECT
     * Необходимо применять только для проверки прав на сущность ProjectEntity
     */
    public static final CustomPermission PENDING_APPROVE = new CustomPermission(1 << 11, 'R'); // 2048

    /**
     * Право на массовую загрузку значений фичефлагов
     * Необходимо применять только для проверки прав на сущность ProjectEntity
     */
    public static final CustomPermission UPLOAD_ENVIRONMENT = new CustomPermission(1 << 12, 'R'); // 4096

    public CustomPermission(int mask) {
        super(mask);
    }

    public CustomPermission(int mask, char code) {
        super(mask, code);
    }

    public boolean isSame(Permission permission) {
        return this.mask == permission.getMask();
    }

}