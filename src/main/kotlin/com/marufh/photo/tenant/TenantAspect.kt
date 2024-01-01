package com.marufh.photo.tenant

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.hibernate.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class TenantAspect {

    @PersistenceContext
    private lateinit var entityManager: EntityManager;

    private val log = LoggerFactory.getLogger(javaClass)

    @Around("execution(public * org.springframework.data.repository.Repository+.*(..))")
    fun enableOwnerFilter(joinPoint: ProceedingJoinPoint): Any {

        val result = joinPoint.proceed()
        if (TenantContext.getCurrentTenant().isEmpty()) {
            log.info("tenant not found")
            return result!!
        }

        try {
            log.info("initializing tenant filter: {}", TenantContext.getCurrentTenant())
            val session: Session = entityManager.unwrap(Session::class.java)
            val filter = session.enableFilter("tenantFilter")
            filter.setParameter("tenant", TenantContext.getCurrentTenant())
            return result!!
        } catch (ex: Exception) {
            log.error("Error enabling ownerFilter : Reason -" + ex.message)
        }
        return Any();
    }
}
