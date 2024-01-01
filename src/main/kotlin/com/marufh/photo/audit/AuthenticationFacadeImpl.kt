package com.marufh.photo.audit

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class AuthenticationFacadeImpl : AuthenticationFacade {
    override val authentication: Authentication get() = SecurityContextHolder.getContext().authentication
}
