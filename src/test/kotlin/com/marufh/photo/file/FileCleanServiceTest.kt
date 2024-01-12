package com.marufh.photo.file

import com.marufh.photo.AbstractServiceTest
import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.entity.FileType
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import util.EncodeUtil
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.time.LocalDate
import kotlin.io.path.Path

class FileCleanServiceTest: AbstractServiceTest() {

    @Test
    fun `clean up empty directory`() {
        File(filePathProperties.media).deleteRecursively()
        File(filePathProperties.thumb).deleteRecursively()

        val base = "/2020/01/01"
        Files.createDirectories(Path(filePathProperties.media + base) )
        assert(Files.exists(Path(filePathProperties.media + base)))
        fileCleanService.cleanEmptyDirectory(Path(filePathProperties.media))
        assert(!Files.exists(Path(filePathProperties.media + base)))
    }

    @Test
    fun `clean up if content exist`() {
        val base = "/2020/01/01"
        File(filePathProperties.media + base).deleteRecursively()

        Files.createDirectories(Path(filePathProperties.media + base) )
        Files.createFile(Path(filePathProperties.media + base + "/test.txt"))

        assert(Files.exists(Path(filePathProperties.media + base)))
        fileCleanService.cleanEmptyDirectory(Path(filePathProperties.media))
        assert(Files.exists(Path(filePathProperties.media + base)))
    }

    @Test
    fun `clean up database entry`() {
        val fileMeta = fileMetaRepository.save(
            FileMeta(name = "test.jpg",
                src = EncodeUtil.encode("/media/2020/02/02/test.jpg"),
                thumb = EncodeUtil.encode("/thumb/2020/02/02/test.jpg"),
                type = FileType.IMAGE,
                base = "/2020/02/02",
                createdAt = LocalDate.now()
            )
        )
        assert(fileMetaRepository.findById(fileMeta.id!!).isPresent)

        // Clean up database entry
        fileCleanService.cleanDatabaseEntry()
        assert(fileMetaRepository.findById(fileMeta.id!!).isEmpty)
    }

    @Test
    fun `clean up database entry if content exist`() {

        albumRepository.deleteAll()
        fileMetaRepository.deleteAll()
        File(filePathProperties.media).deleteRecursively()
        File(filePathProperties.thumb).deleteRecursively()

        val fileMeta = fileUploadService.upload(
            MockMultipartFile(
                "file",
                "car.jpg",
                MediaType.IMAGE_JPEG.toString(),
                FileInputStream("src/test/resources/img/car.jpg")
            )
        )
        assert(fileMetaRepository.findById(fileMeta.id!!).isPresent)

        // Clean up database entry
        fileCleanService.cleanDatabaseEntry()
        assert(fileMetaRepository.findById(fileMeta.id!!).isPresent)
    }
}
