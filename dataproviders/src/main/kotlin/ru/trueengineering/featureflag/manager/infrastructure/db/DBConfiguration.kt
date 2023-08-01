package ru.trueengineering.featureflag.manager.infrastructure.db

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories("ru.trueengineering.featureflag.manager.infrastructure.db.entity")
@EntityScan(basePackages = arrayOf("ru.trueengineering.featureflag.manager.infrastructure.db.entity"))
@ComponentScan("ru.trueengineering.featureflag.manager.infrastructure.db.repositoy")
class DBConfiguration