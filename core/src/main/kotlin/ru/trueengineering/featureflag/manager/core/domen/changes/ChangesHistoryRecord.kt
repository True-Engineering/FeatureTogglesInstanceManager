package ru.trueengineering.featureflag.manager.core.domen.changes

import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.user.User
import java.time.Instant

data class ChangesHistoryRecord (
    var action: ChangeAction,
    var id: Long? = null,
    var project: Project,
    var user: User,
    var environment: Environment? = null,
    var featureFlag: FeatureFlag,
    var created: Instant,
    var featureChanges: FeatureChanges? = null,
    val creationInfo: FeatureFlag? = null
)

enum class ChangeAction {
    CREATE,
    DELETE,
    ENABLE,
    DISABLE,
    EDIT,
    RESTORE
}

data class FeatureChanges(
    val changes: MutableMap<FeatureCompareFields, Difference>? = mutableMapOf()
)

data class Difference(
    var old: Any?,
    var new: Any?
)

enum class FeatureCompareFields(val propertyName: String) {
    DESCRIPTION("description"),
    TAGS("tags"),
    TYPE("type"),
    SPRINT("sprint"),
    GROUP("group"),
    PROPERTIES("properties");

    companion object {

        fun getNames() = values().map { it.propertyName }

        fun findByName(propertyName: String) = values().first { it.propertyName == propertyName }

    }
}