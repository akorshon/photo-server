package com.marufh.photo.file.service

import com.marufh.photo.config.FilePathProperties
import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.entity.FileType
import com.marufh.photo.file.repository.FileMetaRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import util.EncodeUtil
import util.FileUtil
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors

@Service
class FileVisitService(
    val filePathProperties: FilePathProperties,
    val fileMetaRepository: FileMetaRepository) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val fileDatePattern = DateTimeFormatter.ofPattern("yyyy/MMMM/d")
        private var messageDigest: MessageDigest? = null

        init {
            try {
                messageDigest = MessageDigest.getInstance("SHA-512")
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("cannot initialize SHA-512 hash function", e)
            }
        }
    }

    fun visitFile() {
        log.info("Visiting file system to generate data")

        // Check if file meta is already generated
        if (fileMetaRepository.findAll().isNotEmpty()) {
            log.info("File meta already generated")
            return
        }

        val srcDirectoryName: String = Path.of(filePathProperties.base).relativize(Path.of(filePathProperties.media)).toString()
        val start = Instant.now()
        var result: MutableList<FileMeta> = ArrayList()
        try {
            result = Files.walk(Path.of(filePathProperties.media))
                .skip(1)
                .peek { p: Path -> log.info("Visiting file {}", p) }
                .filter { path: Path -> Files.isRegularFile(path) }
                .filter { p: Path -> !p.fileName.toString().startsWith(".") }
                .map { p: Path ->
                    val name = p.fileName
                    val path: Path = Paths.get(filePathProperties.media).relativize(p)
                    val src = "$srcDirectoryName/$path"
                    val size = p.toFile().length()
                    val fileType: FileType =
                        FileUtil.fileTypeMap[FileUtil.getExtension(name.toString())?.lowercase(Locale.getDefault())]!!
                    FileMeta(
                        name = name.toString(),
                        type = fileType,
                        caption = name.toString(),
                        base = path.toString(),
                        src = EncodeUtil.encode(src),
                        thumb = "",
                        createdAt = LocalDate.parse(path.parent.toString(), fileDatePattern),
                        hash = getHash(p.toFile()),
                        size = size,
                    )
                }
                .filter { obj: Any? -> Objects.nonNull(obj) }
                .collect(Collectors.toList())
        } catch (e: IOException) {
            log.error("Error while visiting files", e)
        }

        fileMetaRepository.saveAll(result)
        val end = Instant.now()

        log.info("Visiting files end")
        log.info(Duration.between(start, end).toString())
    }

    private fun getHash(file: File): String {
        val hash: String = try {
            val fi = FileInputStream(file)
            val fileData = ByteArray(file.length().toInt())
            fi.read(fileData)
            fi.close()
            BigInteger(1, messageDigest!!.digest(fileData)).toString(16)
        } catch (e: Exception) {
            log.error("Error while getting hash", e)
            return ""
        }
        return hash
    }

}
