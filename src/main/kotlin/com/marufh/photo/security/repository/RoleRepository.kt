package com.marufh.photo.security.repository

import com.marufh.photo.security.entity.RoleType
import com.marufh.photo.security.entity.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RoleRepository : JpaRepository<Role, String> {
    @Query("SELECT r FROM Role r where r.name=?1")
    fun findByName(name: RoleType): Role
}
