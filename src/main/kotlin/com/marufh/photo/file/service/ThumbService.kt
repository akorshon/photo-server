package com.marufh.photo.file.service

import com.marufh.photo.config.FilePathProperties
import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.entity.FileType
import com.marufh.photo.file.repository.FileMetaRepository
import org.imgscalr.Scalr
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import util.EncodeUtil
import util.FileUtil
import ws.schild.jave.MultimediaObject
import ws.schild.jave.ScreenExtractor
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.imageio.ImageIO

@Service
class ThumbService(
    val filePathProperties: FilePathProperties,
    val fileMetaRepository: FileMetaRepository) {

    val log = LoggerFactory.getLogger(javaClass)

    fun create() {
        log.info("Thumb creation start")
        val start = Instant.now()
        createThumbRecursively(File(filePathProperties.media))
        val end = Instant.now()
        log.info("Done. Thumb creation time: {}", Duration.between(start, end))
    }

    fun create(file: File, thumbLocation: String): File? {
        log.info("Creating thumb: {} for: {}", thumbLocation, file)

        var thumbFile: File? = null
        try {
            Files.createDirectories(File(thumbLocation).toPath().parent)
            val extension: String = FileUtil.getExtension(file.getName())!!
            val fileType: FileType? = FileUtil.fileTypeMap[extension.lowercase(Locale.getDefault())]
            if (fileType == FileType.IMAGE) {
                thumbFile = makeImageThumb(file, thumbLocation, 600, 360)
            } else if (fileType == FileType.VIDEO) {
                thumbFile = makeVideoThumb(file, thumbLocation)
            }
        } catch (e: Exception) {
            log.error("Error creating thumb: {}", e.message)
        }
        return thumbFile
    }

    fun fixThumb(id: String) {
        log.info("Fixing thumb for id: {}", id)

        val fileMeta: FileMeta = fileMetaRepository.findById(id)
            .orElseThrow { RuntimeException("File not found") }

        // Delete existing thumb
        try {
            Files.deleteIfExists(Path.of(filePathProperties.base + "/" + EncodeUtil.decode(fileMeta.thumb)))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        val thumbFile = File(filePathProperties.base + "/" + EncodeUtil.decode(fileMeta.src))
        val file = create(thumbFile, filePathProperties.thumb + fileMeta.base)
        val thumbPath: String = Path.of(filePathProperties.base).relativize(
            Path.of(file!!.absolutePath)
        ).toString()
        log.info("Thumb created: {}", thumbPath)
        fileMeta.thumb = EncodeUtil.encode(thumbPath)
    }

    private fun createThumbRecursively(currentDir: File) {

        val files = currentDir.listFiles()!!
        for (file in files) {
            if (file.isDirectory()) {
                createThumbRecursively(file)
            } else {
                log.info("Processing: {}", file)
                val extension: String = FileUtil.getExtension(file.getName())!!
                if (FileUtil.isInvalidFile(file.getName())) {
                    continue
                }
                val path: String =
                    filePathProperties.thumb + file.getParent().substring(filePathProperties.base.length)
                val fileType: FileType? = FileUtil.fileTypeMap[extension.lowercase(Locale.getDefault())]
                if (fileType == FileType.IMAGE && Files.exists(Path.of(path + "/" + file.getName()))) {
                    log.info("Thumb already exist for the file: {}", file.getName())
                    continue
                }
                if (FileUtil.fileTypeMap[extension.lowercase(Locale.getDefault())] === FileType.IMAGE) {
                    makeImageThumb(file, path, 600, 360)
                } else if (FileUtil.fileTypeMap[extension.lowercase(Locale.getDefault())] === FileType.VIDEO) {
                    log.info("processing video file to create thumb")
                    makeVideoThumb(file, path)
                }
            }
        }
    }

    companion object {
        val log = LoggerFactory.getLogger(javaClass)
        fun makeImageThumb(source: File?, thumbDestinationLocation: String?, width: Int, height: Int): File {
            log.info("Creating image thumb started")
            val finalFile = File(thumbDestinationLocation)
            try {
                val bufferedImage = ImageIO.read(source)
                log.info("Thumb file: {}", finalFile.toPath())
                val thumbnail: BufferedImage =
                    Scalr.resize(bufferedImage, Scalr.Method.SPEED, Scalr.Mode.AUTOMATIC, width, height)
                if (!Files.exists(finalFile.toPath())) {
                    ImageIO.write(thumbnail, "jpg", finalFile)
                }
            } catch (e: Exception) {
                log.error("Resize image error", e)
            }
            return finalFile
        }

        fun makeVideoThumb(source: File?, thumbLocation: String): File {
            log.info("Creating video thumb started for {}", source)
            val finalFile = File("$thumbLocation.jpg")
            try {
                log.info("Creating video thumb started: {}", finalFile.toPath())
                if (!Files.exists(finalFile.toPath())) {
                    val instance = ScreenExtractor()
                    instance.renderOneImage(MultimediaObject(source), -1, -1, 2000, finalFile, 0)
                }
            } catch (e: Exception) {
                log.error("Error on creating video thumb", e)
            }
            return finalFile
        }
    }
}
