package com.marufh.photo.security

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException


@Component("restAuthenticationEntryPoint")
class RestAuthenticationEntryPoint: AuthenticationEntryPoint {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Throws(IOException::class, ServletException::class)
    override fun commence(
        httpServletRequest: HttpServletRequest?,
        httpServletResponse: HttpServletResponse,
        exception: AuthenticationException) {

        logger.error("Responding with unauthorized error. Message - {}", exception.message)
        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.localizedMessage)
    }
}
