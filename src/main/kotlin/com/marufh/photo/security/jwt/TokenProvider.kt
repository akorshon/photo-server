package com.marufh.photo.security.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class TokenProvider {

    private val log = LoggerFactory.getLogger(TokenProvider::class.java)

    companion object {
        private const val tokenStr = ""
        private const val TMP_SIGNATURE_KEY = "tmp-signature-key"
    }

    fun createFileToken(durationInMinutes: Long, tenant: String?): String {
        log.info("Creating tmp for file download")

        val currentTime = LocalDateTime.now()
        val expiryDate = Date.from(currentTime.plusMinutes(durationInMinutes)
            .atZone(ZoneId.systemDefault()).toInstant())
        return Jwts.builder()
            .setSubject(tenant)
            .setIssuedAt(Date.from(currentTime.atZone(ZoneId.systemDefault()).toInstant()))
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, TMP_SIGNATURE_KEY)
            .compact()
    }

    fun validateToken(authToken: String?): Boolean {
        try {
            Jwts.parser().setSigningKey(TMP_SIGNATURE_KEY).parseClaimsJws(authToken)
            return true
        } /*catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        }*/ catch (ex: IllegalArgumentException) {
            log.error("JWT claims string is empty.")
        }
        return false
    }
}
