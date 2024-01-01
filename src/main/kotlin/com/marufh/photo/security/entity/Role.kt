package com.marufh.photo.security.entity

import com.marufh.photo.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "ROLE")
class Role(
    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true)
    var name: RoleType? = null
) : BaseEntity()
