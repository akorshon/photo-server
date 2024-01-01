package com.marufh.photo.album.entity

import com.marufh.photo.entity.BaseTenantAuditEntity
import com.marufh.photo.file.entity.FileMeta
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "ALBUM")
class Album(

    @Column(name = "name")
    var name: String? = null,

    @Column(name = "description")
    var description: String? = null,

    @ManyToOne
    var coverImage: FileMeta? = null,

    @Column(name = "date")
    var date: LocalDate? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    var files: MutableSet<FileMeta> = mutableSetOf()

    ) : BaseTenantAuditEntity()
