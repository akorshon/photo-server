package com.marufh.photo.album.service

import com.marufh.photo.album.dto.AlbumDto
import com.marufh.photo.album.dto.AlbumMapper
import com.marufh.photo.album.entity.Album
import com.marufh.photo.album.entity.AlbumAction
import com.marufh.photo.album.repository.AlbumRepository
import com.marufh.photo.exception.NotFoundException
import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.repository.FileMetaRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class AlbumService(
    //val tokenProvider: TokenProvider,
    val albumMapper: AlbumMapper,
    val albumRepository: AlbumRepository,
    val fileMetaRepository: FileMetaRepository) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun create(albumDto: AlbumDto): AlbumDto {
        log.info("Creating album {}", albumDto)

        return albumMapper.toEntity(albumDto)
            .let { albumRepository.save(it) }
            .let { albumMapper.toDto(it) }
    }

    fun update(albumDto: AlbumDto): AlbumDto {
        log.info("Updating album {}", albumDto)

        return albumRepository.findById(albumDto.id!!)
            .orElseThrow { NotFoundException(String.format("Album not found with id: %s", albumDto.id)) }
            .let {
                it.name = albumDto.name
                it.description = albumDto.description
                it.date = albumDto.date
                it.coverImage = albumDto.coverImage
                albumRepository.save(it) }
            .let { albumMapper.toDto(it) }
    }

    fun findAll(): List<AlbumDto> {
        log.info("Finding all albums")

        return albumRepository.findAllOrderByDate()?.stream()
            ?.map { album ->
                AlbumDto(
                    id = album?.id,
                    name = album?.name!!,
                    description = album.description,
                    coverImage = album.coverImage,
                    date = album.date,
                )
            }?.collect(Collectors.toList()) ?: emptyList()
    }

    fun findById(id: String): AlbumDto {
        log.info("Finding album by id: {}", id)

        return albumRepository.findById(id)
            .map { album ->
                AlbumDto(
                    id = album.id,
                    name = album.name!!,
                    description = album.description,
                    coverImage = album.coverImage,
                    date = album.date,
                )
            }
            .orElseThrow { NotFoundException("Album not found for the id: $id") }
    }

    fun findPhotoByAlbumId(albumId: String): AlbumDto {
        log.info("Find photo by albumId={}", albumId)

        return albumRepository.findByIdWithFiles(albumId)
            .map { albumMapper.toDto(it) }
            .orElseThrow { NotFoundException("Not found") }
    }

    fun manageFile(albumId: String, fileId: String, action: String): AlbumDto {
        log.info("Manage file: albumId={}, fileId={}, action={}", albumId, fileId, action)

        val album: Album = albumRepository.findByIdWithFiles(albumId)
            .orElseThrow { NotFoundException("Not found") }
        val fileMeta: FileMeta = fileMetaRepository.findById(fileId)
            .orElseThrow { NotFoundException("Not found") }

        if (AlbumAction.ADD.name == action) {
            album.files.add(fileMeta)
        } else if (AlbumAction.REMOVE.name  == action) {
            album.files.remove(fileMeta)
        }

        return albumRepository.save(album)
            .let { albumMapper.toDto(it) }
    }

    fun delete(id: String) {
        log.info("Delete album: id={}", id)
        albumRepository.deleteById(id)
    }

    /*private fun addTokenToUrl(fileMeta: FileMeta?): FileMeta? {
        if (fileMeta == null) {
            return null
        }
        val token: String = tokenProvider.createFileToken(10080, TenantContext.getCurrentTenant())
        fileMeta.setThumb(fileMeta.getThumb() + "?token=" + token)
        fileMeta.setSrc(fileMeta.getSrc() + "?token=" + token)
        return fileMeta
    }*/
}
