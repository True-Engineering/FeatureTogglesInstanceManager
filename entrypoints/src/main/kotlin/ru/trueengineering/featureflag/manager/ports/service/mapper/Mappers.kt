package ru.trueengineering.featureflag.manager.ports.service.mapper

import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.ReportingPolicy
import org.springframework.beans.factory.annotation.Autowired
import ru.trueengineering.featureflag.manager.auth.IPermissionService
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangesHistoryRecord
import ru.trueengineering.featureflag.manager.core.domen.changes.Difference
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.environment.Instance
import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.organization.Organization
import ru.trueengineering.featureflag.manager.core.domen.organization.OrganizationUser
import ru.trueengineering.featureflag.manager.core.domen.project.EnvironmentRole
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.project.ProjectUser
import ru.trueengineering.featureflag.manager.core.domen.toggle.EnvironmentFeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.toggle.ImportChanges
import ru.trueengineering.featureflag.manager.core.domen.toggle.ImportEnvironments
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.ports.rest.controller.AgentFeatureFlagDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.ChangesHistoryDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.EnvironmentInfoDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.EnvironmentRoleDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.FeatureFlagDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.FeatureFlagEnvironment
import ru.trueengineering.featureflag.manager.ports.rest.controller.ImportChangesDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.ImportEnvironmentsDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.InstanceDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.OrganizationDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.OrganizationUserDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.ProjectDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.ProjectUserDto
import ru.trueengineering.featureflag.manager.ports.rest.controller.UserDto

interface BaseMapper<DOMAIN, DTO> {

    fun convertToDto(domain: DOMAIN): DTO

    fun convertToDtoList(domains: List<DOMAIN>): List<DTO>
}

@Mapper(componentModel = "spring")
abstract class EnvironmentMapper : BaseMapper<Environment, EnvironmentInfoDto> {

    @Autowired
    var permissionService: IPermissionService? = null

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "authKeyExist", expression = "java(domain.getAuthKeyHash() != null)")
    abstract override fun convertToDto(domain: Environment): EnvironmentInfoDto

    @Mapping(target = "authKeyHash", ignore = true)
    abstract fun convertToDomain(dto: EnvironmentInfoDto): Environment

    abstract fun convertToDto(domain: Instance): InstanceDto

    @AfterMapping
    fun evaluateStatus(@MappingTarget environmentInfoDto: EnvironmentInfoDto) {
        environmentInfoDto.status = EnvironmentConnectionStatus.NOT_CONNECTED
        for (instanceDto in environmentInfoDto.instances) {
            if (instanceDto.status == InstanceConnectionStatus.UNAVAILABLE) {
                environmentInfoDto.status = EnvironmentConnectionStatus.UNAVAILABLE
                break
            }
            if (instanceDto.status == InstanceConnectionStatus.OUT_OF_SYNC) {
                environmentInfoDto.status = EnvironmentConnectionStatus.OUT_OF_SYNC
            }
            if (environmentInfoDto.status != EnvironmentConnectionStatus.OUT_OF_SYNC
                && instanceDto.status == InstanceConnectionStatus.ACTIVE
            ) {
                environmentInfoDto.status = EnvironmentConnectionStatus.ACTIVE
            }
        }
    }

    @AfterMapping
    fun evaluatePermissions(@MappingTarget target: EnvironmentInfoDto, source: Environment) {
        permissionService?.getPermissionsNameListForCurrentUser(source).let { target.permissions = it }
    }

}

@Mapper(componentModel = "spring")
abstract class FeatureFlagMapper : BaseMapper<FeatureFlag, FeatureFlagDto> {

    @Autowired
    var permissionService: IPermissionService? = null

    abstract override fun convertToDto(domain: FeatureFlag): FeatureFlagDto

    @Mapping(target = "flippingStrategy", source = "strategy")
    @Mapping(target = "permissions", ignore = true)
    abstract fun convertToDto(domain: ru.trueengineering.featureflag.manager.core.domen.toggle.Environment): FeatureFlagEnvironment

    @Mapping(target = "strategy", source = "flippingStrategy")
    abstract fun convertToDomain(dto: FeatureFlagEnvironment): ru.trueengineering.featureflag.manager.core.domen.toggle.Environment

    @Mapping(target = "id", ignore = true)
    abstract fun convertToDomain(dto: FeatureFlagDto): FeatureFlag

    abstract fun convertToFeatureFlagEnvironmentDtoList(domains: List<FeatureFlagEnvironment>):
            List<ru.trueengineering.featureflag.manager.core.domen.toggle.Environment>

    abstract fun convertToDomainList(dtos: List<FeatureFlagDto>): List<FeatureFlag>

    @AfterMapping
    fun evaluatePermissions(
        @MappingTarget target: FeatureFlagEnvironment,
        source: ru.trueengineering.featureflag.manager.core.domen.toggle.Environment
    ) {
        permissionService?.getPermissionsNameListForCurrentUser(source).let { target.permissions = it }
    }

}

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
abstract class AgentFeatureFlagMapper : BaseMapper<EnvironmentFeatureFlag, AgentFeatureFlagDto> {

    abstract fun convertToDomain(dto: AgentFeatureFlagDto): EnvironmentFeatureFlag

    abstract fun convertToDomainList(dtos: List<AgentFeatureFlagDto>): List<EnvironmentFeatureFlag>
}

@Mapper(componentModel = "spring", uses = [ProjectMapper::class])
abstract class OrganizationMapper : BaseMapper<Organization, OrganizationDto> {

    @Autowired
    var permissionService: IPermissionService? = null

    @AfterMapping
    fun evaluatePermissions(@MappingTarget target: OrganizationDto, source: Organization) {
        permissionService?.getPermissionsNameListForCurrentUser(source).let { target.permissions = it }
    }

    @Mapping(target = "permissions", ignore = true)
    abstract override fun convertToDto(domain: Organization): OrganizationDto
}

@Mapper(componentModel = "spring", uses = [EnvironmentMapper::class])
abstract class ProjectMapper : BaseMapper<Project, ProjectDto> {

    @Autowired
    var permissionService: IPermissionService? = null

    @AfterMapping
    fun evaluatePermissions(@MappingTarget target: ProjectDto, source: Project) {
        permissionService?.getPermissionsNameListForCurrentUser(source).let { target.permissions = it }
    }

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    abstract override fun convertToDto(domain: Project): ProjectDto

    @AfterMapping
    fun evaluateStatus(@MappingTarget projectDto: ProjectDto) {
        val environmentsStatuses = projectDto.environments.map { it.status }.toHashSet()
        if (environmentsStatuses.isEmpty() || environmentsStatuses.contains(EnvironmentConnectionStatus.NOT_CONNECTED)) {
            projectDto.status = EnvironmentConnectionStatus.NOT_CONNECTED
            return
        }

        if (environmentsStatuses.contains(EnvironmentConnectionStatus.UNAVAILABLE)) {
            projectDto.status = EnvironmentConnectionStatus.UNAVAILABLE
            return
        }
        if (environmentsStatuses.contains(EnvironmentConnectionStatus.OUT_OF_SYNC)) {
            projectDto.status = EnvironmentConnectionStatus.OUT_OF_SYNC
            return
        }
        if (environmentsStatuses.contains(EnvironmentConnectionStatus.ACTIVE)) {
            projectDto.status = EnvironmentConnectionStatus.ACTIVE
            return
        }
        projectDto.status = EnvironmentConnectionStatus.NOT_CONNECTED
    }
}

@Mapper(componentModel = "spring")
interface UserMapper : BaseMapper<User, UserDto> {

    @Mapping(target = "userSettings.defaultProjectId", source = "defaultProjectId")
    override fun convertToDto(domain: User): UserDto
}


@Mapper(componentModel = "spring", uses = [UserMapper::class])
abstract class ProjectUserMapper : BaseMapper<ProjectUser, ProjectUserDto> {

    abstract fun convertToDto(domain: EnvironmentRole): EnvironmentRoleDto

    abstract override fun convertToDto(domain: ProjectUser): ProjectUserDto
}

@Mapper(componentModel = "spring", uses = [UserMapper::class, ProjectUserMapper::class])
abstract class OrganizationUserMapper : BaseMapper<OrganizationUser, OrganizationUserDto>

@Mapper(componentModel = "spring")
abstract class ImportChangesMapper : BaseMapper<ImportChanges, ImportChangesDto> {
    abstract override fun convertToDto(domain: ImportChanges): ImportChangesDto
}

@Mapper(componentModel = "spring")
abstract class ImportEnvironmentsMapper: BaseMapper<ImportEnvironments, ImportEnvironmentsDto> {
    abstract override fun convertToDto(domain: ImportEnvironments): ImportEnvironmentsDto
}

@Mapper(componentModel = "spring", uses = [FeatureFlagMapper::class])
abstract class ChangesHistoryMapper: BaseMapper<ChangesHistoryRecord, ChangesHistoryDto> {
    @Mapping(target = "environment", expression = "java(domain.getEnvironment() != null ? domain.getEnvironment().getName() : null)")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "changes", expression = "java(mapChanges(domain))")
    @Mapping(target = "time", source = "created")
    abstract override fun convertToDto(domain: ChangesHistoryRecord): ChangesHistoryDto

    fun mapChanges(domain: ChangesHistoryRecord): MutableMap<String, Difference> {
        return domain.featureChanges?.changes?.mapKeys { it.key.propertyName }?.toMutableMap() ?: mutableMapOf()
    }
}