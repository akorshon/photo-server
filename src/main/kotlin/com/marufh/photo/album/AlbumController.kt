package com.marufh.photo.album

import com.marufh.photo.album.dto.AlbumDto
import com.marufh.photo.album.entity.Album
import com.marufh.photo.album.service.AlbumService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/private/albums")
class AlbumController(
    val albumService: AlbumService) {

    @PostMapping
    fun create(@RequestBody albumDto: AlbumDto): AlbumDto {
        return albumService.create(albumDto)
    }

    @PutMapping
    fun update(@RequestBody albumDto: AlbumDto): AlbumDto {
        return albumService.update(albumDto)
    }

    @GetMapping
    fun findAll(): List<AlbumDto> {
        return albumService.findAll()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): AlbumDto {
        return albumService.findById(id)
    }

    @GetMapping("/{albumId}/files")
    fun findPhotosById(@PathVariable albumId: String): AlbumDto {
        return albumService.findPhotoByAlbumId(albumId)
    }

    @PostMapping("/{albumId}/manage-file/{fileId}/{action}")
    fun addFile(@PathVariable albumId: String, @PathVariable fileId: String, @PathVariable action: String): AlbumDto {
        return albumService.manageFile(albumId, fileId, action)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) {
        albumService.delete(id)
    }
}
