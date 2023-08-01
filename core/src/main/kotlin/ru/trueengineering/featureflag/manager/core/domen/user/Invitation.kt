package ru.trueengineering.featureflag.manager.core.domen.user

import ru.trueengineering.featureflag.manager.core.domen.project.Project
import java.util.UUID

class Invitation(
    val id: UUID,
    val project: Project
)