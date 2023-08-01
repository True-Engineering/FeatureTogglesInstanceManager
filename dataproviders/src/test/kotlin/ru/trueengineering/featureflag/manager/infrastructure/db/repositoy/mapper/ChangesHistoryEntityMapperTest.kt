package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper

import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangeAction
import ru.trueengineering.featureflag.manager.core.domen.changes.ChangesHistoryRecord
import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.project.Project
import ru.trueengineering.featureflag.manager.core.domen.toggle.FeatureFlag
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ChangesHistoryEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.EnvironmentEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.FeatureFlagEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.ProjectEntity
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.UserEntity
import java.time.Instant

class ChangesHistoryEntityMapperTest(
    @Autowired override var uut: ChangesHistoryEntityMapper
): MapperSpec<ChangesHistoryEntityMapper, ChangesHistoryRecord, ChangesHistoryEntity> {

    override fun verifyEntity(actualEntity: ChangesHistoryEntity) {
        val expected = buildEntity()
        Assertions.assertThat(actualEntity)
            .usingRecursiveComparison()
            .isEqualTo(expected)
    }

    override fun verifyDomain(actual: ChangesHistoryRecord) {
        val expected = buildDomain()
        Assertions.assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected)
    }

    override fun buildDomain(): ChangesHistoryRecord {
        return ChangesHistoryRecord(
            ChangeAction.ENABLE,
            1,
            Project(id = 1, name = "project"),
            User("name", "email", 1),
            Environment(name = "env", id = 1),
            FeatureFlag("uid", id = 1),
            Instant.MAX
        )
    }

    override fun buildEntity(): ChangesHistoryEntity {
        val projectEntity = ProjectEntity("project").apply {
            id = 1
            environments = HashSet()
        }
        val userEntity = UserEntity("email").apply {
            id = 1
            name = "name"
        }
        val environmentEntity = EnvironmentEntity("env").apply {
            id = 1
            emails = HashSet()
            instances = HashSet()
        }
        val featureFlagEntity = FeatureFlagEntity("uid").apply {
            id = 1
            environments = HashSet()
            description = ""
            sprint = ""
            group = ""
        }

        return ChangesHistoryEntity(ChangeAction.ENABLE).apply {
            id = 1
            project = projectEntity
            user = userEntity
            environment = environmentEntity
            featureFlag = featureFlagEntity
            created = Instant.MAX
        }
    }

}