package com.marufh.photo.security.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.marufh.photo.entity.BaseEntity
import com.marufh.photo.entity.BaseTenantEntity
import jakarta.persistence.*

@Entity
@Table(name = "USER", uniqueConstraints = [UniqueConstraint(columnNames = ["tenant", "email"])])
data class User(

    @Column(name = "first_name")
    var firstName: String? = null,

    @Column(name = "last_name")
    var lastName: String? = null,

    @Column(name = "email", nullable = false)
    var email: String = "",

    @Column(name = "password", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var password: String = "",

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "USER_ROLE",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: Set<Role> = emptySet(),

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = false

): BaseTenantEntity()
