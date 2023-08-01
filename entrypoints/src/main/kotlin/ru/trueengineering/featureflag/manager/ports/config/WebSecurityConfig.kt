package ru.trueengineering.featureflag.manager.ports.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import ru.trueengineering.featureflag.manager.ports.config.properties.ClientSecurityProperties
import ru.trueengineering.featureflag.manager.ports.rest.mdc.filter.MDCFilter
import ru.trueengineering.featureflag.manager.ports.security.agent.AgentTokenAuthFilter
import ru.trueengineering.featureflag.manager.ports.security.user.TokenAuthFilter


@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ClientSecurityProperties::class)
class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    private val filter: TokenAuthFilter? = null
    @Autowired
    private val agentFilter: AgentTokenAuthFilter? = null
    @Autowired
    private val mdcFilter: MDCFilter? = null
    @Autowired
    private val clientSecurityProperties: ClientSecurityProperties? = null

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .antMatcher("/**").authorizeRequests()
            .antMatchers(*(clientSecurityProperties?.inSecurityUrls?.toTypedArray() ?: emptyArray())).permitAll()
            .antMatchers("/**")
            .authenticated()
            .and()
            .exceptionHandling()
            .authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            .and()
            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(agentFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(mdcFilter, agentFilter!!::class.java)
    }

}