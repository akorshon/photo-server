package com.marufh.photo.file.service

import com.marufh.photo.config.FilePathProperties
import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.repository.FileMetaRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import util.EncodeUtil
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

@Service
class FileCleanService(
    val filePathProperties: FilePathProperties,
    val fileMetaRepository: FileMetaRepository) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun cleanUp() {
        cleanDatabaseEntry()
        cleanEmptyDirectory(Path.of(filePathProperties.media))
        cleanEmptyDirectory(Path.of(filePathProperties.thumb))
    }

    fun cleanDatabaseEntry() {
        log.info("Cleaning file from database is started")

        var pageRequest: Pageable = PageRequest.of(0, 500)
        var page: Page<FileMeta> = fileMetaRepository.findAll(pageRequest)

        while (!page.isEmpty) {
            pageRequest = pageRequest.next()
            for (fileMeta in page.content) {
                log.info("Checking: {}", fileMeta.name)
                val exist =
                    Files.exists(Path.of(filePathProperties.base + "/" + EncodeUtil.decode(fileMeta.src)))
                if (!exist) {
                    try {
                        log.info("Deleting from database: {}", fileMeta.name)
                        Files.deleteIfExists(Path.of(filePathProperties.base + "/" + EncodeUtil.decode(fileMeta.src)))
                        Files.deleteIfExists(Path.of(filePathProperties.base + "/" + EncodeUtil.decode(fileMeta.thumb)))
                        fileMetaRepository.delete(fileMeta)
                    } catch (e: IOException) {
                        log.error("Error while deleting file", e)
                    }
                }
            }
            page = fileMetaRepository.findAll(pageRequest)
        }

        log.info("Cleaning file from database is finished")
    }

    fun cleanEmptyDirectory(dirPath: Path) {
        log.info("Remove empty directory is started: {}", dirPath)

        try {
            Files.walk(dirPath).use { stream ->
                stream.sorted(Comparator.reverseOrder())
                    .forEach { p: Path ->
                        try {
                            if (Files.isDirectory(p) && Files.list(p).findAny().isEmpty) {
                                log.info("Deleting empty directory: {}", p)
                                Files.deleteIfExists(p)
                            }
                        } catch (e: IOException) {
                            log.error("Error while deleting empty directory", e)
                        }
                    }
            }
        } catch (e: IOException) {
            log.error("Error while deleting empty directory {}", dirPath, e)
        }

        log.info("Remove empty directory is finished: {}", dirPath)
    }
}
