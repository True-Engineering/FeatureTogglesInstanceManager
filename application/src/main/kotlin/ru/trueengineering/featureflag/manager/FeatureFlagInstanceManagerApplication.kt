package ru.trueengineering.featureflag.manager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import ru.trueengineering.featureflag.manager.core.domen.authorize.PermissionFilterConfiguration
import ru.trueengineering.featureflag.manager.infrastructure.db.DBConfiguration
import ru.trueengineering.featureflag.manager.ports.config.EntryPointsConfiguration

@Import(value = [EntryPointsConfiguration::class, DBConfiguration::class, PermissionFilterConfiguration::class])
@SpringBootApplication
class FeatureFlagInstanceManagerApplication

fun main(args: Array<String>) {
	runApplication<FeatureFlagInstanceManagerApplication>(*args)
}
