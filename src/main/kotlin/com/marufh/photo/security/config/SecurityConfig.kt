package com.marufh.photo.security.config

import com.marufh.photo.security.RestAuthenticationEntryPoint
import com.marufh.photo.security.service.TokenService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class SecurityConfig(
    val tokenService: TokenService,
    val authenticationEntryPoint: RestAuthenticationEntryPoint) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        logger.info("Configuring security filter chain")

        httpSecurity
            .csrf { csrf -> csrf.disable() }
            .exceptionHandling { exception -> exception.authenticationEntryPoint(authenticationEntryPoint) }
            .cors{ cors -> cors.disable()}
            .sessionManagement{session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)}
            .authorizeHttpRequests { auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/registration").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
            }
            .authorizeHttpRequests { auth -> auth
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasRole( "USER")
                .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.authenticationManager { authManager ->
                        val auth = authManager as BearerTokenAuthenticationToken
                        val user = tokenService.parseToken(auth.token) ?: throw InvalidBearerTokenException("Invalid token")
                        UsernamePasswordAuthenticationToken(user, null, listOf(user.roles.map { SimpleGrantedAuthority(it.name?.name) }).flatten())
                    }
                }
            }

        return httpSecurity.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        logger.info("Configuring cors")

        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
        configuration.allowedHeaders = listOf("authorization", "content-type")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
