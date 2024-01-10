package com.marufh.photo.file.entity

import com.marufh.photo.entity.BaseTenantEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "FILE_META")
class FileMeta(

    @Column(name = "name")
    var name: String,

    @Column(name = "base")
    var base: String,

    @Column(name = "src")
    var src: String,

    @Column(name = "thumb")
    var thumb: String,

    @Column(name = "file_type")
    @Enumerated(EnumType.STRING)
    var type: FileType,

    @Column(name = "created_at")
    var createdAt: LocalDate,

    @Column(name = "caption")
    var caption: String? = null,

    @Column(name = "size")
    var size: Long? = null,

    @Column(name = "hash")
    var hash: String? = null,

    @Column(name = "favorite", columnDefinition = "tinyint(1)")
    var favorite: Boolean = false,

    @Column(name = "deleted", columnDefinition = "tinyint(1)")
    var deleted: Boolean = false,

    @Column(name = "archived", columnDefinition = "tinyint(1)")
    var archived: Boolean = false,

) : BaseTenantEntity()

