package com.marufh.photo.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.GenericGenerator
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity(
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36, name = "id") var id: String? = null

) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }
}
