package com.marufh.photo.audit

import org.springframework.security.core.Authentication

interface AuthenticationFacade {
    val authentication: Authentication?
}
