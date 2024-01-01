package com.marufh.photo.config

import com.marufh.photo.audit.AuditorAwareCustom
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@Configuration
@EnableJpaAuditing
class AuditConfig {

    @Bean
    fun auditorProvider(): AuditorAwareCustom {
        return AuditorAwareCustom()
    }
}
