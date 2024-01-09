package com.marufh.photo.file.service

import com.marufh.photo.album.entity.Album
import com.marufh.photo.album.repository.AlbumRepository
import com.marufh.photo.config.FilePathProperties
import com.marufh.photo.exception.NotFoundException
import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.entity.FileType
import com.marufh.photo.file.repository.FileMetaRepository
import com.marufh.photo.security.jwt.TokenProvider
import com.marufh.photo.tenant.TenantContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import util.EncodeUtil
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

@Service
class FileMetaService(
    val tokenProvider: TokenProvider,
    val fileMetaRepository: FileMetaRepository,
    val filePathProperties: FilePathProperties,
    val albumRepository: AlbumRepository) {

    companion object {
        private var tokenStr = ""
    }

    private val log = LoggerFactory.getLogger(javaClass)

    fun getFileMeta(date: LocalDate, name: String, type: FileType, pageable: Pageable): Page<FileMeta> {
        log.info("getting all file meta for date: {}", date)

        val page: PageRequest = PageRequest.of(pageable.pageNumber, 100)
        val files: Page<FileMeta> = fileMetaRepository.getFileByDateAndName(date, name, type, page)
        return addTokenToUrl(files)
    }

    fun favorite(id: String, favorite: Boolean): FileMeta {
        val fileMeta: FileMeta = fileMetaRepository
            .findById(id)
            .orElseThrow { NotFoundException("File not found") }
        fileMeta.favorite = favorite
        val updated: FileMeta = fileMetaRepository.save(fileMeta)
        return addTokenToUrl(updated)
    }

    fun getArchived(pageable: Pageable): Page<FileMeta> {
        val fileMetaPage: Page<FileMeta> = fileMetaRepository.findArchived(pageable)
        return addTokenToUrl(fileMetaPage)
    }

    fun getDeleted(pageable: Pageable): Page<FileMeta> {
        val fileMetaPage: Page<FileMeta> = fileMetaRepository.findDeleted(pageable)
        return addTokenToUrl(fileMetaPage)
    }

    fun getFavorite(pageable: Pageable): Page<FileMeta> {
        val fileMetaPage: Page<FileMeta> = fileMetaRepository.findFavorite(pageable)
        return addTokenToUrl(fileMetaPage)
    }

    fun restore(id: String): FileMeta {
        val fileMeta: FileMeta = fileMetaRepository
            .findById(id)
            .orElseThrow { NotFoundException("File not found: $id") }
        fileMeta.deleted = false
        return fileMetaRepository.save(fileMeta)
    }

    fun archive(id: String): FileMeta {
        val fileMeta: FileMeta = fileMetaRepository
            .findById(id)
            .orElseThrow { NotFoundException("File not found: $id") }
        fileMeta.archived =  true
        return fileMetaRepository.save(fileMeta)
    }

    fun delete(id: String) {
        log.info("deleting file: $id")

        val fileMeta: FileMeta = fileMetaRepository
            .findById(id)
            .orElseThrow { NotFoundException("File not found: $id") }


        // Delete from album
        val albums: List<Album> = albumRepository.fineByFileId(id)
        for (album in albums) {
            album.files.remove(fileMeta)
            albumRepository.save(album)
        }

        if (fileMeta.deleted) {
            log.info("deleting file permanently: {}", id)

            try {
                if (fileMeta.src != null) {
                    Files.deleteIfExists(Path.of(filePathProperties.base + "/" + EncodeUtil.decode(fileMeta.src)))
                }
                if (fileMeta.thumb != null) {
                    Files.deleteIfExists(Path.of(filePathProperties.base + "/" + EncodeUtil.decode(fileMeta.thumb)))
                }
                fileMetaRepository.delete(fileMeta)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            // Just put a delete flag on DB entry
            fileMeta.deleted = true
            fileMetaRepository.save(fileMeta)
        }
    }

    private fun addTokenToUrl(fileMetaPage: Page<FileMeta>): Page<FileMeta> {
        val token: String
        if (tokenProvider.validateToken(tokenStr)) {
            token = tokenStr
        } else {
            token = tokenProvider.createFileToken(10080, TenantContext.getCurrentTenant())
            tokenStr = token
        }
        for (fileMeta in fileMetaPage.getContent()) {
            fileMeta.src = fileMeta.src + "?token=" + token
            fileMeta.thumb = fileMeta.thumb + "?token=" + token
        }
        return PageImpl(fileMetaPage.getContent(), fileMetaPage.getPageable(), fileMetaPage.getTotalElements())
    }

    private fun addTokenToUrl(files: List<FileMeta>): List<FileMeta> {
        val token: String
        if (tokenProvider.validateToken(tokenStr)) {
            token = tokenStr
        } else {
            token = tokenProvider.createFileToken(10080, TenantContext.getCurrentTenant())
            tokenStr = token
        }
        for (fileMeta in files) {
            fileMeta.src = fileMeta.src + "?token=" + token
            fileMeta.thumb  = fileMeta.thumb + "?token=" + token
        }
        return files
    }

    private fun addTokenToUrl(fileMeta: FileMeta): FileMeta {
        val token: String = tokenProvider.createFileToken(10080, TenantContext.getCurrentTenant())
        fileMeta.src = fileMeta.src + "?token=" + token
        fileMeta.thumb = fileMeta.thumb + "?token=" + token
        return fileMeta
    }
}
