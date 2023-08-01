package ru.trueengineering.featureflag.manager.core.domen.project

import org.springframework.security.access.prepost.PreAuthorize
import java.util.EnumMap

interface UpdateProjectUseCase {

    @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'EDIT')")
    fun execute(command: UpdateProjectCommand): Project

}

data class UpdateProjectCommand(
    val projectName: String,
    val projectId: Long,
    val organizationId: Long,
    val properties: ProjectProperties = EnumMap(ProjectPropertiesClass::class.java)
)