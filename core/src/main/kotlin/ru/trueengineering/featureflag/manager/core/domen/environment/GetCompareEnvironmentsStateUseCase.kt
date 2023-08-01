package ru.trueengineering.featureflag.manager.core.domen.environment

import org.springframework.security.access.prepost.PreAuthorize
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag

interface GetCompareEnvironmentsStateUseCase {

    @PreAuthorize(
            "(hasPermission(#command.from, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'EDIT') || " +
            "hasPermission(#command.from, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'READ_ENVIRONMENT')) && " +
            "(hasPermission(#command.to, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'EDIT') || " +
            "hasPermission(#command.to, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment', 'READ_ENVIRONMENT')) && " +
            "hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'READ_PROJECT')"
    )
    fun execute(command: GetCompareEnvironmentsStateCommand): CompareLists

}

data class GetCompareEnvironmentsStateCommand(
    val projectId: Long,
    val from: Long,
    val to: Long
)

data class CompareLists(
    val enable: List<FeatureFlag>,
    val disable: List<FeatureFlag>
)