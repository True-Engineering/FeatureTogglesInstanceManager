package ru.trueengineering.featureflag.manager.ports.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@ComponentScan("ru.trueengineering.featureflag.manager.ports")
@EnableAspectJAutoProxy
@Configuration
class EntryPointsConfiguration