package com.marufh.photo.file.entity

import com.marufh.photo.entity.BaseTenantEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "FILE_META")
class FileMeta(

    @Column(name = "name")
    var name: String? = null,

    @Column(name = "base")
    var base: String? = null,

    @Column(name = "src")
    private var src: String? = null,

    @Column(name = "thumb")
    private var thumb: String? = null,

    @Column(name = "caption")
    private var caption: String? = null,

    @Column(name = "created_at")
    private var createdAt: LocalDate? = null,

    @Column(name = "file_type")
    @Enumerated(EnumType.STRING)
    private var type: FileType? = null,

    @Column(name = "size")
    private var size: Long? = null,

    @Column(name = "hash")
    private var hash: String? = null,

    @Column(name = "favorite", columnDefinition = "tinyint(1)")
    var favorite: Boolean = false,

    @Column(name = "deleted", columnDefinition = "tinyint(1)")
    var deleted: Boolean = false,

    @Column(name = "archived", columnDefinition = "tinyint(1)")
    var archived: Boolean = false,

) : BaseTenantEntity()

