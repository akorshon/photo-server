package com.marufh.photo.file

import com.marufh.photo.AbstractServiceTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate

class FileCountServiceTest: AbstractServiceTest() {

    @Test
    fun `file count by date`() {
        // Delete photo folder
        File(filePathProperties.base + "/photo").deleteRecursively()
        albumRepository.deleteAll()
        fileMetaRepository.deleteAll()


        fileUploadService.upload(MockMultipartFile(
            "file",
            "car.jpg",
            MediaType.IMAGE_JPEG.toString(),
            FileInputStream("src/test/resources/img/car.jpg")
        ))

        fileUploadService.upload(MockMultipartFile(
            "file",
            "mountain.jpg",
            MediaType.IMAGE_JPEG.toString(),
            FileInputStream("src/test/resources/img/mountain.jpg")
        ))

        fileUploadService.upload(MockMultipartFile(
            "file",
            "sea.jpg",
            MediaType.IMAGE_JPEG.toString(),
            FileInputStream("src/test/resources/img/sea.jpg")
        ))

        val fileCounterList =  fileCountService.fileCountByDate()
        assert(fileCounterList[0].count == 3)
        assert(fileCounterList[0].day == LocalDate.now().dayOfMonth)
        assert(fileCounterList[0].year == LocalDate.now().year)
        assert(fileCounterList[0].month == LocalDate.now().monthValue)
    }
}
