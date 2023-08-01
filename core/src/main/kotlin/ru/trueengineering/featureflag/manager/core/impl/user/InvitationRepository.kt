package ru.trueengineering.featureflag.manager.core.impl.user

import ru.trueengineering.featureflag.manager.core.domen.user.Invitation
import java.util.UUID

interface InvitationRepository {

    fun create(uuid: UUID, projectId: Long): Invitation

    fun findByProject(projectId: Long): Invitation?

    fun findById(invitationId: UUID): Invitation?

}
