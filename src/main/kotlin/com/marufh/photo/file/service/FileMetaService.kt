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
import util.FileUtil
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.*

@Service
class FileMetaService(
    val tokenProvider: TokenProvider,
    val fileMetaRepository: FileMetaRepository,
    val filePathProperties: FilePathProperties,
    val albumRepository: AlbumRepository) {

    companion object {
        private var tokenStr = ""
        private var messageDigest: MessageDigest? = null
        init {
            try {
                messageDigest = MessageDigest.getInstance("SHA-512")
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("cannot initialize SHA-512 hash function", e)
            }
        }
    }

    private val log = LoggerFactory.getLogger(javaClass)

    fun getFileMeta(date: LocalDate?, name: String?, type: FileType?, pageable: Pageable): Page<FileMeta> {
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

    fun fixCreateDateByName() {
        try {
            var pageRequest: Pageable = PageRequest.of(0, 500)
            var onePage: Page<FileMeta> = fileMetaRepository.findAll(pageRequest)
            var count = 0
            while (!onePage.isEmpty()) {
                pageRequest = pageRequest.next()
                for (fileMeta in onePage.getContent()) {
                    if (FileUtil.DATE_PATTERN.matcher(fileMeta.name).find()) {
                        var name: String = fileMeta?.name!!
                        log.info("Name: {}", name)

                        // Remove prefix
                        if (name.startsWith("VID_")) {
                            name = name.replace("VID_", "")
                        }

                        // Remove prefix
                        if (name.startsWith("IMG")) {
                            name = name.replace("IMG_", "")
                        }

                        // Get year, month and day
                        val dateStr = name.substring(0, 4) + "-" + name.substring(4, 6) + "-" + name.substring(6, 8)
                        var nameDate: LocalDate
                        nameDate = try {
                            LocalDate.parse(dateStr)
                        } catch (e: DateTimeParseException) {
                            log.warn("Invalid date: {}", dateStr)
                            continue
                        }
                        if (fileMeta.createdAt.isEqual(nameDate)) {
                            log.warn("Created date: {} and name date are equal: {}", fileMeta.createdAt, dateStr)
                            continue
                        }
                        log.info("Date on file name: {}", dateStr)
                        log.info("Created date: {}", fileMeta.createdAt)
                        val pathInsideTarget = nameDate.year.toString() + "/" + nameDate.month.getDisplayName(
                            TextStyle.FULL,
                            Locale.ENGLISH
                        ) + "/" + nameDate.dayOfMonth
                        val newSrcDir: String = filePathProperties.media + pathInsideTarget
                        if (!Files.exists(Path.of(newSrcDir))) {
                            log.info("Creating path: {}", newSrcDir)
                            Files.createDirectories(Path.of(newSrcDir))
                        }
                        val newThmDir: String = filePathProperties.thumb + pathInsideTarget
                        if (!Files.exists(Path.of(newThmDir))) {
                            log.info("Creating path: {}", newThmDir)
                            Files.createDirectories(Path.of(newThmDir))
                        }

                        // For source
                        val finalSrcPath = newSrcDir + "/" + fileMeta.name
                        if (!Files.exists(Path.of(finalSrcPath))) {
                            val sourcePath: Path = Path.of(filePathProperties.media + fileMeta.base)
                            val destinationPath = Path.of(finalSrcPath)
                            if (Files.exists(sourcePath)) {
                                Files.move(sourcePath, destinationPath)
                            }
                            log.info("{} moved to  {}", sourcePath, destinationPath)
                        }

                        // For thumb
                        val finalThumbPath = newThmDir + "/" + fileMeta.name
                        if (!Files.exists(Path.of(finalThumbPath))) {
                            val sourcePath: Path = Path.of(filePathProperties.thumb + fileMeta.base)
                            val destinationPath = Path.of(finalThumbPath)
                            if (Files.exists(sourcePath)) {
                                Files.move(sourcePath, destinationPath)
                            }
                            log.info("{} moved to  {}", sourcePath, destinationPath)
                        }
                        val basePath = pathInsideTarget + "/" + fileMeta.name
                        val src: String =
                            Path.of(filePathProperties.base).relativize(Path.of(filePathProperties.media))
                                .toString()
                        val thumb: String =
                            Path.of(filePathProperties.base).relativize(Path.of(filePathProperties.thumb))
                                .toString()
                        fileMeta.name = name
                        fileMeta.base = basePath
                        fileMeta.src = EncodeUtil.encode("$src/$basePath")
                        fileMeta.thumb = EncodeUtil.encode(
                            prepareThumb(
                                thumb,
                                Path.of(basePath),
                                Path.of(fileMeta.name)
                            )
                        )

                        fileMeta.createdAt = nameDate
                        log.info("{}", fileMeta)
                        fileMetaRepository.save(fileMeta)
                        count++
                        println("----------------------------------------")
                    }
                }
                onePage = fileMetaRepository.findAll(pageRequest)
            }
            log.info("Total file changed: $count")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun generateHash() {
        log.info("Generating hash is started ")
        var pageRequest: Pageable = PageRequest.of(0, 500)
        var onePage: Page<FileMeta> = fileMetaRepository.findAll(pageRequest)
        while (!onePage.isEmpty()) {
            pageRequest = pageRequest.next()
            for (fileMeta in onePage.content) {
                if (fileMeta?.hash != null) {
                    continue
                }
                log.info("Generating hash for: {}", fileMeta?.name)
                try {
                    val file = File(filePathProperties.media + fileMeta?.base)
                    val fi = FileInputStream(file)
                    val fileData = ByteArray(file.length().toInt())
                    fi.read(fileData)
                    fi.close()
                    val hash = BigInteger(1, messageDigest!!.digest(fileData)).toString(16)
                    fileMeta?.hash = hash
                    fileMetaRepository.save(fileMeta)
                } catch (e: Exception) {
                    log.warn("Error generating hash: {}", e.message)
                }
            }
            onePage = fileMetaRepository.findAll(pageRequest)
        }
        log.info("Generating hash is finished")
    }

    private fun prepareThumb(thumb: String, path: Path, name: Path): String {
        var thumbFile = "$thumb/$path"
        if (FileUtil.fileTypeMap.get(FileUtil.getExtension(name.toString())?.lowercase(Locale.getDefault())) === FileType.VIDEO) {
            thumbFile = "$thumbFile.jpeg"
        }
        return thumbFile
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

    private fun addTokenToUrl(fileMeta: FileMeta): FileMeta {
        val token: String = tokenProvider.createFileToken(10080, TenantContext.getCurrentTenant())
        fileMeta.src = fileMeta.src + "?token=" + token
        fileMeta.thumb = fileMeta.thumb + "?token=" + token
        return fileMeta
    }
}
