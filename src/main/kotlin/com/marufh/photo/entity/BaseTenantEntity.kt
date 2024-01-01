package com.marufh.photo.entity

import com.marufh.photo.tenant.TenantContext.getCurrentTenant
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.validation.constraints.NotBlank
import org.hibernate.annotations.Filter
import org.hibernate.annotations.FilterDef
import org.hibernate.annotations.ParamDef

/**
 * Created by maruf on 4/7/17.
 */
@FilterDef(name = "tenantFilter", parameters = [ParamDef(name = "tenant", type = String::class)])
@Filter(name = "tenantFilter", condition = "tenant = :tenant ")
@MappedSuperclass
abstract class BaseTenantEntity(

    @NotBlank
    @Column(length = 36, name = "tenant", nullable = false, updatable = false)
    var tenant: String? = null,

) : BaseEntity() {

    @PrePersist
    fun onPrePersist() {
        tenant = getCurrentTenant()
    }


}
