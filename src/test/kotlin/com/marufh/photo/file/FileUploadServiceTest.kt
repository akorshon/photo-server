package com.marufh.photo.file

import com.marufh.photo.AbstractServiceTest
import com.marufh.photo.exception.AlreadyExistException
import com.marufh.photo.exception.FileUploadException
import com.marufh.photo.file.entity.FileType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.io.FileInputStream


class FileUploadServiceTest: AbstractServiceTest() {

    @Test
    fun `test upload file`() {
        // Delete photo folder
        File(filePathProperties.base + "/photo").deleteRecursively()

        val file = MockMultipartFile(
            "file",
            "car.jpg",
            MediaType.IMAGE_JPEG.toString(),
            FileInputStream("src/test/resources/img/car.jpg")
        )

        val fileMeta = fileUploadService.upload(file)
        assert(fileMeta.id != null)
        assert(fileMeta.name == "car.jpg")
        assert(fileMeta.type == FileType.IMAGE)
        assert(fileMeta.favorite == false)
        assert(fileMeta.deleted == false)
        assert(fileMeta.archived == false)
        assert(fileMeta.tenant == "test")
    }

    @Test
    fun `test upload file already exist exception`() {
        // Delete photo folder
        File(filePathProperties.base + "/photo").deleteRecursively()

        val file = MockMultipartFile(
            "file",
            "car.jpg",
            MediaType.IMAGE_JPEG.toString(),
            FileInputStream("src/test/resources/img/car.jpg")
        )

        val fileMeta = fileUploadService.upload(file)
        assert(fileMeta.id != null)
        assert(fileMeta.name == "car.jpg")
        assert(fileMeta.type == FileType.IMAGE)
        assert(fileMeta.favorite == false)
        assert(fileMeta.deleted == false)
        assert(fileMeta.archived == false)
        assert(fileMeta.tenant == "test")

        // upload same file again
        assertThrows<AlreadyExistException> {
            fileUploadService.upload(MockMultipartFile(
                "file",
                "car.jpg",
                MediaType.IMAGE_JPEG.toString(),
                FileInputStream("src/test/resources/img/car.jpg")
            ))
        }
    }

    @Test
    fun `test upload file with invalid extension`() {
        val file = MockMultipartFile(
            "file",
            "car.txt",
            MediaType.TEXT_PLAIN.toString(),
            "This is a nice car".toByteArray()
        )
        assertThrows<FileUploadException> { fileUploadService.upload(file) }
    }
}
