package com.marufh.photo.audit

import org.slf4j.LoggerFactory
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import java.util.*

class AuditorAwareCustom : AuditorAware<String> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getCurrentAuditor(): Optional<String> {
        val securityContext = SecurityContextHolder.getContext()
        if (securityContext?.authentication == null) {
            log.error("No user authenticated")
            return Optional.of("Anonymous")
        }
        val user: User = securityContext.authentication.principal as User
        return Optional.of(user.username)
    }
}
