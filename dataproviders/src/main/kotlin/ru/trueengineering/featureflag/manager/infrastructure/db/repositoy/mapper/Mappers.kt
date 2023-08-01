package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValueMappingStrategy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PostFilter
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangesHistoryRecord
import ru.trueengineering.featureflag.manager.core.domen.changes.FeatureChanges
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.environment.Instance
import ru.trueengineering.featureflag.manager.core.domen.organization.Organization
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.toggle.Strategy
import ru.trueengineering.featureflag.manager.core.domen.user.Invitation
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ChangesHistoryEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EnvironmentEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureChangesEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagEnvironmentStateEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagEnvironmentStatePK
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.InstanceEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.InvitationEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.OrganizationEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ProjectEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.UserEntity

interface BaseEntityMapper<ENTITY, DOMAIN> {

    fun convertToDomain(entity: ENTITY): DOMAIN

    fun convertToDomainList(domains: Iterable<ENTITY>): List<DOMAIN>

    fun convertToEntityList(domains: Iterable<DOMAIN>): List<ENTITY>

    fun convertToEntity(domain: DOMAIN): ENTITY
}

@Mapper(componentModel = "spring")
interface EnvironmentEntityMapper : BaseEntityMapper<EnvironmentEntity, Environment> {

    fun convertToDomain(entity: InstanceEntity): Instance

    @Mapping(target = "created", ignore = true)
    @Mapping(target = "environment", ignore = true)
    fun convertToEntity(domain: Instance): InstanceEntity

    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "removed", ignore = true)
    override fun convertToEntity(domain: Environment): EnvironmentEntity

    @PostFilter("hasPermission(filterObject, 'READ_ENVIRONMENT') or hasPermission(filterObject, 'EDIT') or hasAuthority('AGENT')")
    override fun convertToDomainList(domains: Iterable<EnvironmentEntity>): List<Environment>

}

@Mapper(componentModel = "spring", uses = [FeatureFlagEnvironmentEntityMapper::class])
abstract class FeatureFlagEntityMapper : BaseEntityMapper<FeatureFlagEntity, FeatureFlag> {

    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "removed", ignore = true)
    @Mapping(target = "tag", expression = "java(domain.getTags().stream().findAny().orElse(null))")
    abstract override fun convertToEntity(domain: FeatureFlag): FeatureFlagEntity

    @Mapping(
        target = "tags", expression = "java(entity.getTag() == null ? java.util.Collections.emptySet() : " +
                "java.util.Set.of(entity.getTag()))"
    )
    abstract override fun convertToDomain(entity: FeatureFlagEntity): FeatureFlag

    @AfterMapping
    fun sortEnvironments(@MappingTarget target: FeatureFlag) {
        target.environments.sortBy { it.id }
    }
}

@Mapper(componentModel = "spring")
abstract class FeatureFlagEnvironmentEntityMapper :
    BaseEntityMapper<FeatureFlagEnvironmentStateEntity,
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment> {

    @Autowired
    var objectMapper: ObjectMapper? = null

    override fun convertToDomain(entity: FeatureFlagEnvironmentStateEntity):
            ru.trueengineering.featureflag.manager.core.domen.toggle.Environment {
        return ru.trueengineering.featureflag.manager.core.domen.toggle.Environment(
            entity.primaryKey?.environment?.id!!.toLong(),
            entity.primaryKey?.environment?.name!!,
            entity.enable,
            entity.strategy?.let { Strategy(it, stringToMap(entity.strategyParams)) }
        )
    }

    override fun convertToEntity(domain: ru.trueengineering.featureflag.manager.core.domen.toggle.Environment):
            FeatureFlagEnvironmentStateEntity {
        return FeatureFlagEnvironmentStateEntity(domain.enable).apply {
            this.strategy = domain.strategy?.type
            this.strategyParams = objectMapper?.writeValueAsString(domain.strategy?.initParams)
            this.primaryKey = FeatureFlagEnvironmentStatePK(EnvironmentEntity(domain.name)
                .apply { id = domain.id })
        }
    }

    @PostFilter("hasPermission(filterObject, 'READ_ENVIRONMENT') or hasPermission(filterObject, 'EDIT') or hasAuthority('AGENT')")
    abstract override fun convertToDomainList(domains: Iterable<FeatureFlagEnvironmentStateEntity>):
            List<ru.trueengineering.featureflag.manager.core.domen.toggle.Environment>

    private fun stringToMap(string: String?): MutableMap<String, String> =
        objectMapper?.readValue(string, object : TypeReference<MutableMap<String, String>>() {}) ?: mutableMapOf()
}

@Mapper(componentModel = "spring", uses = [ProjectEntityMapper::class])
interface OrganizationEntityMapper : BaseEntityMapper<OrganizationEntity, Organization> {

    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "removed", ignore = true)
    override fun convertToEntity(domain: Organization): OrganizationEntity

    @PostFilter("hasPermission(filterObject, 'READ_ORGANIZATION') or hasPermission(filterObject, 'EDIT')")
    override fun convertToDomainList(domains: Iterable<OrganizationEntity>): List<Organization>
}

@Mapper(componentModel = "spring", uses = [EnvironmentEntityMapper::class])
abstract class ProjectEntityMapper : BaseEntityMapper<ProjectEntity, Project> {

    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "removed", ignore = true)
    @Mapping(target = "organization", ignore = true)
    abstract override fun convertToEntity(domain: Project): ProjectEntity

    @Mapping(target = "featureFlagsCount", ignore = true)
    @Mapping(target = "membersCount", ignore = true)
    abstract override fun convertToDomain(entity: ProjectEntity): Project

    @PostFilter("hasPermission(filterObject, 'READ_PROJECT') or hasPermission(filterObject, 'EDIT')")
    abstract override fun convertToDomainList(domains: Iterable<ProjectEntity>): List<Project>

    @AfterMapping
    fun sortEnvironments(@MappingTarget target: Project) {
        target.environments.sortBy { it.id }
    }
}

@Mapper(componentModel = "spring", uses = [ProjectEntityMapper::class])
interface InvitationEntityMapper : BaseEntityMapper<InvitationEntity, Invitation> {

    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    override fun convertToEntity(domain: Invitation): InvitationEntity
}

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface UserEntityMapper : BaseEntityMapper<UserEntity, User> {

    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "removed", ignore = true)
    @Mapping(target = "name", source = "userName")
    override fun convertToEntity(domain: User): UserEntity

    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "userName", source = "name")
    @Mapping(target = "authorities", expression = "java(java.util.Collections.emptyList())")
    override fun convertToDomain(entity: UserEntity): User

    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "removed", ignore = true)
    @Mapping(target = "name", source = "userName")
    fun updateEntity(@MappingTarget entity: UserEntity, domain: User)
}

@Mapper(componentModel = "spring")
interface FeatureChangesEntityMapper : BaseEntityMapper<FeatureChangesEntity, FeatureChanges> {

    override fun convertToEntity(domain: FeatureChanges): FeatureChangesEntity

}

@Mapper(
    componentModel = "spring",
    uses = [
        ProjectEntityMapper::class,
        FeatureFlagEntityMapper::class,
        UserEntityMapper::class,
        EnvironmentEntityMapper::class,
        FeatureChangesEntityMapper::class
    ]
)
interface ChangesHistoryEntityMapper : BaseEntityMapper<ChangesHistoryEntity, ChangesHistoryRecord> {

    @Mapping(target = "updated", ignore = true)
    override fun convertToEntity(domain: ChangesHistoryRecord): ChangesHistoryEntity
}