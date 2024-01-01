package com.marufh.photo.album.dto

import com.marufh.photo.album.entity.Album
import org.springframework.stereotype.Component

@Component
class AlbumMapper {

    /* TODO Replace by mapstruct */

    fun toDto(album: Album): AlbumDto {
        return AlbumDto(
            id = album.id,
            name = album.name!!,
            description = album.description,
            coverImage = album.coverImage,
            date = album.date,
            files = album.files,
        )
    }

    fun toEntity(albumDto: AlbumDto): Album {
        val album = Album(
            name = albumDto.name,
            description = albumDto.description,
            coverImage = albumDto.coverImage,
            date = albumDto.date,
        )
        album.id = albumDto.id
        return album
    }
}
