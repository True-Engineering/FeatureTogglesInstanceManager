package ru.trueengineering.featureflag.manager.ports.config

import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import ru.trueengineering.featureflag.manager.ports.config.properties.ClientSecurityProperties
import ru.trueengineering.featureflag.manager.ports.security.user.JwtTokenProvider
import ru.trueengineering.featureflag.manager.ports.security.user.MockTokenProvider
import ru.trueengineering.featureflag.manager.ports.security.user.TokenAuthFilter
import ru.trueengineering.featureflag.manager.ports.security.user.TokenProvider
import ru.trueengineering.featureflag.manager.ports.security.user.UserSecurityService

@Configuration
@EnableConfigurationProperties(ClientSecurityProperties::class)
class SecurityConfiguration {

    @Bean
    fun jwtParser(): JwtParser {
        return Jwts.parserBuilder()
                .build()
    }

    @Bean
    @Profile("!dev")
    fun jwtTokenProvider(
            @Autowired jwtParser: JwtParser,
            @Autowired properties: ClientSecurityProperties
    ): TokenProvider = JwtTokenProvider(jwtParser, properties)

    @Bean
    fun tokenAuthFilter(@Autowired tokenProvider: TokenProvider,
                        @Autowired userSecurityService: UserSecurityService
    ): TokenAuthFilter =
            TokenAuthFilter(userSecurityService, tokenProvider)

    @Bean
    @Profile("dev")
    fun mockTokenProvider(): TokenProvider = MockTokenProvider()
}
