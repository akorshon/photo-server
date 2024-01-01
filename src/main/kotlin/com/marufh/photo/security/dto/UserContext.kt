package com.marufh.photo.security.dto

import com.marufh.photo.security.entity.User
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.util.StringUtils
import java.util.stream.Collectors

class UserContext(
    val email: String,
    val authorities: List<GrantedAuthority>,
    val tenant: String) {

    companion object {

        fun create(email: String, authorities: List<GrantedAuthority>, tenant: String): UserContext {
            require(!StringUtils.hasText(email)) { "Email is blank: $email" }
            return UserContext(email, authorities, tenant)
        }

        fun create(user: User): UserContext {
            if (user.roles.isEmpty() || user.tenant == null) {
                throw InsufficientAuthenticationException("User has no roles or tenant assigned")
            }

            val roles: List<GrantedAuthority> = user.roles.stream()
                .map { authority -> SimpleGrantedAuthority(authority.name?.name) }
                .collect(Collectors.toList())
            return UserContext(user.email, roles, user.tenant!!)
        }
    }
}
