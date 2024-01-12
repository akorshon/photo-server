package com.marufh.photo.file.service

import com.marufh.photo.config.FilePathProperties
import com.marufh.photo.exception.FileUploadException
import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.entity.FileType
import com.marufh.photo.file.repository.FileMetaRepository
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
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
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class FileServiceBase(
    val fileMetaRepository: FileMetaRepository,
    val filePathProperties: FilePathProperties) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun deleteFile(file: File) {
        try {
            Files.deleteIfExists(file.toPath())
        } catch (e: IOException) {
            throw FileUploadException("Error while deleting tmp file: " + e.message)
        }
    }

    fun saveOnTmpDirectory(multipartFile: MultipartFile): File {
        log.info("Saving file on tmp directory: {}", multipartFile.originalFilename)
        return try {
            val tmpPath = Path.of(filePathProperties.tmp).resolve(multipartFile.originalFilename!!)
            // Create parent directory if not exist
            if (!Files.exists(tmpPath.parent)) {
                Files.createDirectories(tmpPath.parent)
            }

            // Copy file to tmp location
            Files.copy(multipartFile.inputStream, tmpPath)
            tmpPath.toFile()
        } catch (e: Exception) {
            throw FileUploadException("Error while saving file on tmp directory: " + e.message)
        }
    }

    fun fileExist(fileDestinationLocation: String): Boolean {
        return Files.exists(Path.of(fileDestinationLocation))
    }

    fun moveOnFinalDestination(file: File, fileDestinationLocation: String?): Path {
        return try {
            log.info("{} moved to  {}", file.getCanonicalPath(), fileDestinationLocation)
            val fileDestinationLocationPath = Path.of(fileDestinationLocation)

            // Create parent directory if not exist
            Files.createDirectories(fileDestinationLocationPath.parent)

            // Move file to destination location
            Files.move(Path.of(file.getCanonicalPath()), fileDestinationLocationPath)
        } catch (e: Exception) {
            throw FileUploadException("Error while moving file to final destination: " + e.message)
        }
    }

    fun saveFile(file: File, dateDirectory: String): FileMeta {
        val name = file.getName()
        val src: String = EncodeUtil.encode(
            Path.of(filePathProperties!!.base).relativize(
                Path.of(
                    filePathProperties.media
                )
            ).toString() + "/" + dateDirectory + "/" + name
        )
        var thumb = Path.of(filePathProperties.base).relativize(
            Path.of(
                filePathProperties.thumb
            )
        ).toString() + "/" + dateDirectory + "/" + name
        val fileType: FileType = FileUtil.fileTypeMap.get(FileUtil.getExtension(name)?.lowercase())!!
        thumb = EncodeUtil.encode(prepareThumb(thumb, fileType))
        val hash = getHash(file)
        val createdDate = LocalDate.parse(dateDirectory, fileDatePattern)
        return fileMetaRepository.save(
            FileMeta(
                name = name,
                type = fileType,
                caption = "",
                base = "$dateDirectory/$name",
                src = src,
                thumb = thumb,
                createdAt = createdDate,
                size = file.length(),
                hash = hash
            )
        )
    }

    fun getMedia(ext: String): MediaType {
        val mime: String? = FileUtil.mimeTypeMapping[ext.lowercase(Locale.getDefault())]
        return if (mime != null) {
            MediaType.valueOf(mime)
        } else MediaType.APPLICATION_OCTET_STREAM
    }

    private fun getHash(file: File): String {
        var hash = ""
        hash = try {
            val fi = FileInputStream(file)
            val fileData = ByteArray(file.length().toInt())
            fi.read(fileData)
            fi.close()
            BigInteger(1, messageDigest!!.digest(fileData)).toString(16)
        } catch (e: IOException) {
            throw FileUploadException("Error while calculating hash: " + e.message)
        }
        return hash
    }

    private fun prepareThumb(thumb: String, type: FileType): String {
        var thumb = thumb
        if (type.equals(FileType.VIDEO)) {
            thumb = "$thumb.jpg"
        }
        return thumb
    }

    companion object {
        private val fileDatePattern = DateTimeFormatter.ofPattern("yyyy/MMMM/d")
        private var messageDigest: MessageDigest? = null

        init {
            try {
                messageDigest = MessageDigest.getInstance("SHA-512")
            } catch (e: NoSuchAlgorithmException) {
                throw FileUploadException("cannot initialize SHA-512 hash function")
            }
        }
    }
}
