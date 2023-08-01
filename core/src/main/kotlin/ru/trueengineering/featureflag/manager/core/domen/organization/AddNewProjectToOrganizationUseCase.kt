package ru.trueengineering.featureflag.manager.core.domen.project

import org.springframework.security.access.prepost.PreAuthorize
import java.util.EnumMap

interface AddNewProjectToOrganizationUseCase {

    @PreAuthorize("hasPermission(#command.organizationId, 'ru.trueengineering.featureflag.manager.core.domen.organization.Organization', 'CREATE_PROJECT')")
    fun execute(command: CreateProjectCommand) : Project

}

data class CreateProjectCommand(
    val projectName: String,
    val organizationId: Long,
    val properties: ProjectProperties = EnumMap(ProjectPropertiesClass::class.java)
)