package com.marufh.photo.file

import com.marufh.photo.AbstractServiceTest
import com.marufh.photo.exception.UnauthorizedException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import util.EncodeUtil
import java.io.File
import java.io.FileInputStream
import java.time.Duration

class FileDownloadServiceTest: AbstractServiceTest() {

    @Test
    fun `download file`() {
        // Delete photo folder
        File(filePathProperties.base + "/photo").deleteRecursively()

        val file = MockMultipartFile(
            "file",
            "car.jpg",
            MediaType.IMAGE_JPEG.toString(),
            FileInputStream("src/test/resources/img/car.jpg")
        )

        val fileMeta = fileUploadService.upload(file)
        val resource = fileDownloadService.getFileAsResource(fileMeta.src, tokenProvider.createFileToken(Duration.ofHours(1).toMinutes(),"test"))
        assert(resource.body != null)
        assert(resource.body!!.contentLength() > 0)
        assert(resource.body!!.filename == "car.jpg")
    }

    @Test
    fun `download file invalid token exception`() {
        assertThrows<UnauthorizedException> {
            fileDownloadService.getFileAsResource("/any/file", "invalid-token")
        }
    }

    @Test
    fun `download file when image not found`() {
        val resource  = fileDownloadService.getFileAsResource(EncodeUtil.encode("/media/file/not/exist.jpg"), tokenProvider.createFileToken(Duration.ofHours(1).toMinutes(),"test"))
        assert(resource.body != null)
        assert(resource.body!!.filename == "image-default-thumb.png")
    }

    @Test
    fun `download file when video not found`() {
        val resource  = fileDownloadService.getFileAsResource(EncodeUtil.encode("/media/file/not/exist.mp4"), tokenProvider.createFileToken(Duration.ofHours(1).toMinutes(),"test"))
        assert(resource.body != null)
        assert(resource.body!!.filename == "video-default-thumb.jpg")
    }

    @Test
    fun `download file when image name has no extension`() {
        val resource  = fileDownloadService.getFileAsResource(EncodeUtil.encode("/media/file/not/exist"), tokenProvider.createFileToken(Duration.ofHours(1).toMinutes(),"test"))
        assert(resource.body != null)
        assert(resource.body!!.filename == "image-default-thumb.png")
    }
}
