package com.marufh.photo.tenant

object TenantContext {

    private val currentTenant = ThreadLocal<String>()

    fun setCurrentTenant(tenant: String) {
        currentTenant.set(tenant)
    }

    fun getCurrentTenant(): String {
        return currentTenant.get()
    }

    fun clear() {
        currentTenant.remove()
    }
}
