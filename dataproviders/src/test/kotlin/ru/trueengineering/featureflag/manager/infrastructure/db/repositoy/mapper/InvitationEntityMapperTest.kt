package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper

import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.user.Invitation
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.InvitationEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ProjectEntity
import java.util.UUID

val id: UUID = UUID.randomUUID()

class InvitationEntityMapperTest(@Autowired override var uut: InvitationEntityMapper)
    : MapperSpec<InvitationEntityMapper, Invitation, InvitationEntity> {

    override fun verifyEntity(actualEntity: InvitationEntity) {
        assertThat(actualEntity).isNotNull
        assertThat(actualEntity.id).isEqualTo(id)
        assertThat(actualEntity.project.id).isEqualTo(1)
    }

    override fun verifyDomain(actual: Invitation) {
        assertThat(actual).isNotNull
        assertThat(actual.id).isEqualTo(id)
        assertThat(actual.project.id).isEqualTo(1)
    }

    override fun buildDomain(): Invitation {
        return Invitation(id, Project(1L, "name", mutableListOf()))
    }

    override fun buildEntity(): InvitationEntity {
        val entity = InvitationEntity(id)
        entity.project = ProjectEntity("name").apply { id = 1L }
        return entity
    }
}