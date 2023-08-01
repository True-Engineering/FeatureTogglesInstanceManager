package ru.trueengineering.featureflag.manager.infrastructure.db.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import java.util.UUID

class InvitationJpaRepositoryTest(@Autowired override var uut: InvitationJpaRepository,
                                  @Autowired var projectRepository: ProjectJpaRepository)
    : JpaRepositoryBaseTest<InvitationJpaRepository>() {

    @Test
    @Sql("/organization_project_dataset.sql")
    internal fun `should create invitation`() {
        val uuid = UUID.randomUUID()
        assertThat(uut.findById(uuid)).isEmpty

        val projectEntity = projectRepository.getById(1)

        val invitationEntity = InvitationEntity(uuid)
        invitationEntity.project = projectEntity!!
        assertThat(uut.save(invitationEntity)).isNotNull

        assertThat(uut.findById(uuid)).isPresent
    }

    @Test
    @Sql("/organization_project_dataset.sql", "/invitation_dataset.sql")
    internal fun `should get invitation by project id`() {
        val actual = uut.findByProjectId(1)
        assertThat(actual).isPresent
        assertThat(actual.get().id).isEqualTo(UUID.fromString("d906e36f-f781-460f-ba29-0e9a2b93887b"))
    }

    @Test
    @Sql("/organization_project_dataset.sql", "/invitation_dataset.sql")
    internal fun `should not get invitation by project id`() {
        val actual = uut.findByProjectId(2)
        assertThat(actual).isEmpty
    }

}