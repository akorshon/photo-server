package util

import com.drew.imaging.ImageMetadataReader
import com.drew.imaging.ImageProcessingException
import com.drew.metadata.Metadata
import com.drew.metadata.exif.*
import com.marufh.photo.file.entity.FileType
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*
import java.util.regex.Pattern


class FileUtil private constructor() {
    init {
        throw IllegalStateException("Utility class")
    }



    companion object {
        private val log = LoggerFactory.getLogger(javaClass)

        val DATE_PATTERN = Pattern.compile(
            "^VID_\\d{8}_\\d{6}.*$" +
                    "|^IMG_\\d{8}_\\d{6}.*$" +
                    "|^\\d{8}_\\d{6}.*$"
        )

        val fileTypeMap: Map<String, FileType> = mapOf(
            "jpg" to FileType.IMAGE,
            "jpeg" to FileType.IMAGE,
            "png" to FileType.IMAGE,
            "mov" to FileType.VIDEO,
            "mp4" to FileType.VIDEO
        )

        val allowExtension: List<String?> = listOf(
            "jpg",
            "jpeg",
            "png",
            "mov",
            "mp4"
        )

        val mimeTypeMapping = mapOf(
            "jpg" to  MediaType.IMAGE_JPEG_VALUE,
            "jpeg" to MediaType.IMAGE_JPEG_VALUE,
            "png" to MediaType.IMAGE_PNG_VALUE,
            "mov" to "video/quicktime",
            "mp4" to "video/mp4"
        )

        fun isInvalidFile(file: String): Boolean {
            return file.isBlank() || invalidName(file) || invalidExtension(file)
        }

        fun createdDate(file: File): String {
            val localDate: LocalDate
            var name = file.getName()
            if (DATE_PATTERN.matcher(name).find()) {
                if (name.startsWith("VID_")) {
                    name = name.replace("VID_", "")
                } else if (name.startsWith("IMG_")) {
                    name = name.replace("IMG_", "")
                }

                // Get year, month and day
                val dateStr = name.substring(0, 4) + "-" + name.substring(4, 6) + "-" + name.substring(6, 8)
                localDate = LocalDate.parse(dateStr)
            } else {
                localDate = getCreatedDate(file)!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            }
            return localDate.year.toString() + "/" + localDate.month.getDisplayName(
                TextStyle.FULL,
                Locale.ENGLISH
            ) + "/" + localDate.dayOfMonth
        }

        fun getSize(file: File?): IntArray {
            try {
                val metadata = ImageMetadataReader.readMetadata(file)
                val directory: ExifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
                val descriptor = ExifSubIFDDescriptor(directory)
                log.info("Width: {}", descriptor.getExifImageWidthDescription())
                log.info("Height: {}", descriptor.getExifImageHeightDescription())
            } catch (e: Exception) {
                log.error("Fail to get size", e)
            }
            return intArrayOf(0, 0)
        }

        fun getCreatedDate(file: File): Date? {
            var date: Date? = null
            try {
                val metadata = ImageMetadataReader.readMetadata(file)
                val directory: ExifSubIFDDirectory? = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
                if (directory != null) {
                    date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
                }
                if (date == null) {
                    log.info("Exif fail to get creation time and date")
                    val attr = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
                    date = Date.from(attr.lastModifiedTime().toInstant())
                }
            } catch (e: IOException) {
                log.error("Fail to determine the created date. Adding default date as today.")
                date = Date(Instant.now().toEpochMilli())
            } catch (e: ImageProcessingException) {
                log.error("Fail to determine the created date. Adding default date as today.")
                date = Date(Instant.now().toEpochMilli())
            }
            log.info("Created date: {}", date)
            return date
        }

        fun getContentType(file: File, request: HttpServletRequest): String {
            var contentType: String = request.getServletContext().getMimeType(file.absolutePath)
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE
            }
            return contentType
        }

        fun invalidName(name: String): Boolean {
            return name.isBlank() || name.startsWith(".") || name.startsWith("~")
        }

        fun invalidExtension(name: String): Boolean {
            return name.isBlank() || !allowExtension.contains(getExtension(name))
        }

        fun invalidExtension(allowExtension: List<String?>, extension: String?): Boolean {
            return extension!!.isEmpty() || !allowExtension.contains(extension.lowercase(Locale.getDefault()))
        }

        fun getName(fileName: String): String {
            return Optional.ofNullable(fileName)
                .filter { file: String -> file.contains(".") }
                .map { file: String -> file.substring(0, file.lastIndexOf(".")) }
                .orElse(fileName)
        }

        fun getExtension(filename: String): String? {
            return Optional.ofNullable(filename)
                .filter { f: String -> f.contains(".") }
                .map { f: String -> f.substring(filename.lastIndexOf(".") + 1).lowercase(Locale.getDefault()) }
                .orElse(null)
        }

        fun validateFileNameAndExt(fileName: String): Boolean {
            log.info("Validating file name: {}", fileName)
            val name = getName(fileName)
            if (invalidName(name)) {
                log.error("Ignored file processing due to bad name: {}", name)
                return false
            }
            val extension = getExtension(fileName)
            if (invalidExtension(allowExtension, extension)) {
                log.error("Ignored file processing due to extension: {}", extension)
                return false
            }
            return true
        }
    }
}
