package com.marufh.photo.audit

import com.marufh.photo.security.dto.UserContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import java.util.*

class AuditorAwareCustom : AuditorAware<String> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getCurrentAuditor(): Optional<String> {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        if (!authentication.isAuthenticated) {
            log.error("No user authenticated")
            return Optional.empty()
        }
        val user: User = authentication.principal as User
        return Optional.of(user.username)
    }
}
