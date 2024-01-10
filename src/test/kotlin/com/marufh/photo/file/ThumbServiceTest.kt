package com.marufh.photo.file

import com.marufh.photo.AbstractServiceTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.io.FileInputStream

class ThumbServiceTest: AbstractServiceTest() {

    @Test
    fun `test thumb create`() {

        File("src/test/resources/img/car-thumb.jpg").delete()
        thumbService.create(
            File("src/test/resources/img/car.jpg"),
            "src/test/resources/img/car-thumb.jpg"
        )

        assert(File("src/test/resources/img/car-thumb.jpg").exists())
    }

    @Test
    fun `test fix thumb`() {

        // Delete photo folder
        File(filePathProperties.base + "/photo").deleteRecursively()

        // Upload file
        val file = MockMultipartFile(
            "file",
            "car.jpg",
            MediaType.IMAGE_JPEG.toString(),
            FileInputStream("src/test/resources/img/car.jpg")
        )
        val fileMeta = fileUploadService.upload(file)

        // Fix thumb
        thumbService.fixThumb(fileMeta.id!!)
        assert(File(filePathProperties.thumb + fileMeta.base).exists())
    }
}
