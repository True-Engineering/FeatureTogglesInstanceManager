package ru.trueengineering.featureflag.manager.core.domen.toggle

import org.springframework.security.access.prepost.PreAuthorize
import java.util.EnumMap

interface EditFeatureFlagUseCase {

        @PreAuthorize("hasPermission(#command.projectId, 'ru.trueengineering.featureflag.manager.core.domen.project.Project', 'CREATE_FLAG')")
        fun execute(command: EditFeatureFlagCommand): FeatureFlag

}

data class EditFeatureFlagCommand(
        val uuid: String,
        val projectId: Long,
        var description: String? = "",
        var group: String? = "",
        var type: FeatureFlagType? = null,
        var sprint: String? = "",
        var tags: Set<String> = HashSet(),
        var properties: FeatureFlagProperties = EnumMap(FeatureFlagPropertiesClass::class.java)
)