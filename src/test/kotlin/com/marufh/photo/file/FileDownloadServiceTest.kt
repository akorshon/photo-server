package com.marufh.photo.file

import com.marufh.photo.AbstractServiceTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.io.FileInputStream
import java.time.Duration

class FileDownloadServiceTest: AbstractServiceTest() {

    @Test
    fun `test download file`() {
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
}
