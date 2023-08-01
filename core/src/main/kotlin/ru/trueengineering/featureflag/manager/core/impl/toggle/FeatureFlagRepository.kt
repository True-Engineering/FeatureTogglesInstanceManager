package ru.trueengineering.featureflag.manager.core.impl.toggle

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangeAction
import ru.trueengineering.featureflag.manager.core.domen.changes.FeatureChanges
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag

interface FeatureFlagRepository {

    /**
     * Получает фичафлаг, который помечен как removed (removed = true), по его uid и id проекта
     */
    fun getRemovedByUidAndProjectId(uid: String, projectId: Long): FeatureFlag?

    /**
     * Получает фичафлаг, который не помечен как removed (removed = false), по его uid и id проекта
     */
    fun getActiveByUidAndProjectId(uid: String, projectId: Long): FeatureFlag?

    /**
     * Получает фичафлаг вне зависимости от поля removed по его uid и id проекта
     */
    fun getDespiteRemovedByUidAndProjectId(uid: String, projectId: Long): FeatureFlag?

    /**
     * Получает список всех активных фичафлагов для проекта по его id
     */
    fun getFeatureFlagsForProject(projectId: Long): List<FeatureFlag>

    /**
     * Получает список всех активных фичафлагов, которые не изменялись как минимум N дней, для проекта по его id
     */
    fun getFeatureFlagsForProjectUpdatedBefore(projectId: Long, days: Int): List<FeatureFlag>

    /**
     * Получает количество активных фичафлагов для проекта по его id
     */
    fun getFeatureFlagsCountForProject(projectId: Long): Long

    /**
     * Получает список всех фичафлагов для окружения (фичафлаг имеет значение true или false на этом окружении) по его id
     */
    fun getFeatureFlagsForEnvironment(environmentId: Long): List<FeatureFlag>

    /**
     * Создает новый фичефгал или изменяет уже существующий, и сохраняет его в базе
     */
    fun createOrEdit(
        featureFlag: FeatureFlag,
        project: Project,
        action: ChangeAction? = null,
        environmentId: Long? = null,
        changes: FeatureChanges? = null,
        creationInfo: FeatureFlag? = null
    ): FeatureFlag

    /**
     * Создает новые фичефгали или изменяет уже существующие, и сохраняет их в базе
     */
    fun saveAll(features: List<FeatureFlag>, project: Project) : List<FeatureFlag>

    /**
     * Если фичафлаг не помечен как removed (removed = false), удаляет все его связи с окружениями и помечает как removed (removed = true)
     */
    fun deleteFeatureFlag(featureFlagUid: String, projectId: Long)

    /**
     * Если фичафлаг помечен как removed (removed = true), восстанавливает его (removed = false)
     */
    fun activateFeatureFlag(featureFlagUid: String, projectId: Long)

    /**
     * Удаляет все связи с фичафлагами у окружения по его id
     */
    fun deleteFeatureEnvironment(environmentId: Long)

    /**
     * Ищет все фичафлаги, описание или uid которых соответствуют шаблону
     */
    fun getFeatureFlagsByPattern(projectId: Long, pattern: String, pageable: Pageable): Page<FeatureFlag>

    /**
     * Ищем все теги фичафлагов определенного проекта
     */
    fun getTags(projectId: Long, pageable: Pageable): Page<String>
}