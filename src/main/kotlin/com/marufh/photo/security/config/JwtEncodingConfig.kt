package com.marufh.photo.security.config

import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.spec.SecretKeySpec

@Configuration
class JwtEncodingConfig(
    @Value("\${auth.jwt.key}")
    private val jwtKey: String) {
        private val secretKey = SecretKeySpec(jwtKey.toByteArray(), "HmacSHA256")

        @Bean
        fun jwtDecoder(): JwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).build()

        @Bean
        fun jwtEncoder(): JwtEncoder = NimbusJwtEncoder(ImmutableSecret(secretKey))
}
