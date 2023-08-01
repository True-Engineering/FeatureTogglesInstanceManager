package ru.trueengineering.featureflag.manager.ports.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "feature-flag.auth.client")
data class ClientSecurityProperties(

    val inSecurityUrls: List<String> = emptyList(),

    val adminAuthorityRoleName: String = "ff-portal-admins",

    val userNameClaim: String = "given_name",

    val userEmailClaim: String = "email"
)
