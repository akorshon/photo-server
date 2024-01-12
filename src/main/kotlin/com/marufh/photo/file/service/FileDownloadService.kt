package com.marufh.photo.file.service

import com.marufh.photo.config.FilePathProperties
import com.marufh.photo.exception.UnauthorizedException
import com.marufh.photo.file.entity.FileType
import com.marufh.photo.security.jwt.TokenProvider
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.UrlResource
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import util.EncodeUtil
import util.FileUtil
import java.io.IOException
import java.nio.file.Path
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class FileDownloadService(
    private val tokenProvider: TokenProvider,
    private val resourceLoader: ResourceLoader,
    private val fileServiceBase: FileServiceBase,
    private val filePathProperties: FilePathProperties) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun getFileAsResource(encodedPath: String?, token: String?): ResponseEntity<Resource> {
        if (!tokenProvider.validateToken(token)) {
            throw UnauthorizedException("Invalid Token")
        }

        val decodePhotoPath: String = EncodeUtil.decode(encodedPath)
        val path = Path.of(filePathProperties.base, decodePhotoPath)
        log.info("Downloading file: {}", path)

        val ext: String = FileUtil.getExtension(decodePhotoPath)
            ?: return ResponseEntity.status(HttpStatus.OK)
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                .contentType(MediaType.IMAGE_PNG)
                .body(resourceLoader.getResource("classpath:image-default-thumb.png"))
        var resource: Resource? = null
        try {
            resource = UrlResource(path.toUri())
            if (!resource.exists()) {
                log.error("Resource not exist: {}", path)
                val fileType: FileType? = FileUtil.fileTypeMap[ext.lowercase(Locale.getDefault())]
                if (fileType == FileType.IMAGE) {
                    resource = resourceLoader.getResource("classpath:image-default-thumb.png")
                } else if (fileType == FileType.VIDEO) {
                    resource = resourceLoader.getResource("classpath:video-default-thumb.jpg")
                }
            }
        } catch (e: IOException) {
            log.error("Loading image failed, {}", e.message)
        }

        return ResponseEntity.status(HttpStatus.OK)
            .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
            .contentType(fileServiceBase.getMedia(ext))
            .body(resource)
    }
}
