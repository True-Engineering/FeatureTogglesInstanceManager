package ru.trueengineering.featureflag.manager.core.domen.project

import ru.trueengineering.featureflag.manager.auth.BusinessEntity
import ru.trueengineering.featureflag.manager.authorization.CustomRole
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.domen.user.UserRole
import java.io.Serializable
import java.util.EnumMap

typealias ProjectProperties = MutableMap<ProjectPropertiesClass, String?>

data class Project(
    var id: Long? = null,
    var name: String,
    var environments: MutableList<Environment> = mutableListOf(),
    var featureFlagsCount: Long? = null,
    var membersCount: Long? = null,
    var properties: ProjectProperties = EnumMap(ProjectPropertiesClass::class.java)
) : BusinessEntity {
    override fun getBusinessId(): Serializable? = id

    override fun getType(): String = this.javaClass.name
}

data class ProjectUser(
    val projectId: Long,
    val projectName: String,
    val user: User,
    val projectRole: CustomRole,
    val environmentPermissions: List<EnvironmentRole>,
)

data class EnvironmentRole(
    val environmentId: Long? = null,
    val environment: String,
    val environmentRole: UserRole
)

enum class ProjectPropertiesClass {

}