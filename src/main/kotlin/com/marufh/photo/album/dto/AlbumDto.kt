package com.marufh.photo.album.dto

import com.marufh.photo.file.entity.FileMeta
import java.time.LocalDate

data class AlbumDto(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val coverImage: FileMeta? = null,
    val date: LocalDate? = null,
    val files: MutableSet<FileMeta> = mutableSetOf()
)
