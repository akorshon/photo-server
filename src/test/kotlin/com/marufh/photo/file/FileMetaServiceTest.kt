package com.marufh.photo.file

import com.marufh.photo.exception.NotFoundException
import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.entity.FileType
import com.marufh.photo.file.repository.FileMetaRepository
import com.marufh.photo.file.service.FileMetaService
import com.marufh.photo.tenant.TenantContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import util.EncodeUtil
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class FileMetaServiceTest {

    @Autowired
    lateinit var fileMetaService: FileMetaService

    @Autowired
    lateinit var fileMetaRepository: FileMetaRepository

    @BeforeEach
    fun setUp() {
        TenantContext.setCurrentTenant("test");
    }

    @Test
    fun `get file meta`() {

        fileMetaRepository.deleteAll();
        val fileMeta = listOf(FileMeta(
            name = "test1",
            type = FileType.IMAGE,
            createdAt = LocalDate.now(),
            base = "/base",
            caption = "caption",
            src = EncodeUtil.encode("src"),
            thumb = EncodeUtil.encode("thumb"),
            size = 100,
            favorite = false,
            deleted = false,
            archived = false
        ),
        FileMeta(
            name = "test2",
            type = FileType.IMAGE,
            createdAt = LocalDate.now(),
            base = "/base",
            caption = "caption",
            src = "/src",
            thumb = "/thumb",
            size = 100,
            favorite = false,
            deleted = false,
            archived = false
        )).let { fileMetaRepository.saveAll(it) }


        // get all images
        val fileMetas = fileMetaService.getFileMeta(
            date = LocalDate.now(),
            name = "",
            type = FileType.IMAGE,
            pageable = PageRequest.of(0, 100)
        )
        assert(fileMetas.content.size == 2)

        // filter by name
        val fileMetas1 = fileMetaService.getFileMeta(
            date = LocalDate.now(),
            name = "test1",
            type = FileType.IMAGE,
            pageable = PageRequest.of(0, 100)
        )
        assert(fileMetas1.content.size == 1)


        // filter by name
        val fileMetas2 = fileMetaService.getFileMeta(
            date = LocalDate.now(),
            name = "test2",
            type = FileType.IMAGE,
            pageable = PageRequest.of(0, 100)
        )
        assert(fileMetas2.content.size == 1)

        // filter by type
        val fileMetas3 = fileMetaService.getFileMeta(
            date = LocalDate.now(),
            name = "test2",
            type = FileType.VIDEO,
            pageable = PageRequest.of(0, 100)
        )
        assert(fileMetas3.content.size == 0)


        // filter by date
        val fileMetas4 = fileMetaService.getFileMeta(
            date = LocalDate.now().minusDays(1),
            name = "test2",
            type = FileType.VIDEO,
            pageable = PageRequest.of(0, 100)
        )
        assert(fileMetas4.content.size == 0)
    }

    @Test
    fun `test favorite`() {
        fileMetaRepository.deleteAll();
        val fileMeta = listOf(FileMeta(
            name = "test1",
            type = FileType.IMAGE,
            createdAt = LocalDate.now(),
            base = "/base",
            caption = "caption",
            src = EncodeUtil.encode("src"),
            thumb = EncodeUtil.encode("thumb"),
            size = 100,
            favorite = false,
            deleted = false,
            archived = false
        )).let { fileMetaRepository.saveAll(it) }

        val updated = fileMetaService.favorite(fileMeta[0].id!!, true)
        assert(updated.favorite)
    }

    @Test
    fun `test favorite exception`() {
        assertThrows<NotFoundException> { fileMetaService.favorite("id-not-existed", true) }
    }

    @Test
    fun `test get archive file`() {
        fileMetaRepository.deleteAll();
        val fileMeta = listOf(FileMeta(
            name = "test1",
            type = FileType.IMAGE,
            createdAt = LocalDate.now(),
            base = "/base",
            caption = "caption",
            src = EncodeUtil.encode("src"),
            thumb = EncodeUtil.encode("thumb"),
            size = 100,
            favorite = false,
            deleted = false,
            archived = true
        ),
        FileMeta(
            name = "test2",
            type = FileType.IMAGE,
            createdAt = LocalDate.now(),
            base = "/base",
            caption = "caption",
            src = EncodeUtil.encode("src"),
            thumb = EncodeUtil.encode("thumb"),
            size = 100,
            favorite = false,
            deleted = false,
            archived = true
        )).let { fileMetaRepository.saveAll(it) }

        val updated = fileMetaService.getArchived(PageRequest.of(0, 100))
        assert(updated.content.size == 2)
    }


    @Test
    fun `test get deleted file`() {
        fileMetaRepository.deleteAll();
        val fileMeta = listOf(FileMeta(
            name = "test1",
            type = FileType.IMAGE,
            createdAt = LocalDate.now(),
            base = "/base",
            caption = "caption",
            src = EncodeUtil.encode("src"),
            thumb = EncodeUtil.encode("thumb"),
            size = 100,
            favorite = false,
            deleted = true,
            archived = true
        ),
            FileMeta(
                name = "test2",
                type = FileType.IMAGE,
                createdAt = LocalDate.now(),
                base = "/base",
                caption = "caption",
                src = EncodeUtil.encode("src"),
                thumb = EncodeUtil.encode("thumb"),
                size = 100,
                favorite = false,
                deleted = true,
                archived = true
            )).let { fileMetaRepository.saveAll(it) }

        val updated = fileMetaService.getDeleted(PageRequest.of(0, 100))
        assert(updated.content.size == 2)
    }

    @Test
    fun `test get favourite file`() {
        fileMetaRepository.deleteAll();
        val fileMeta = listOf(FileMeta(
            name = "test1",
            type = FileType.IMAGE,
            createdAt = LocalDate.now(),
            base = "/base",
            caption = "caption",
            src = EncodeUtil.encode("src"),
            thumb = EncodeUtil.encode("thumb"),
            size = 100,
            favorite = true,
            deleted = false,
            archived = true
        ),
            FileMeta(
                name = "test2",
                type = FileType.IMAGE,
                createdAt = LocalDate.now(),
                base = "/base",
                caption = "caption",
                src = EncodeUtil.encode("src"),
                thumb = EncodeUtil.encode("thumb"),
                size = 100,
                favorite = true,
                deleted = false,
                archived = true
            )).let { fileMetaRepository.saveAll(it) }

        val updated = fileMetaService.getFavorite(PageRequest.of(0, 100))
        assert(updated.content.size == 2)
    }

    @Test
    fun `test restore file`() {
        fileMetaRepository.deleteAll();
        val fileMeta = listOf(FileMeta(
            name = "test1",
            type = FileType.IMAGE,
            createdAt = LocalDate.now(),
            base = "/base",
            caption = "caption",
            src = EncodeUtil.encode("src"),
            thumb = EncodeUtil.encode("thumb"),
            size = 100,
            favorite = true,
            deleted = true,
            archived = true
        )).let { fileMetaRepository.saveAll(it) }

        val updated = fileMetaService.restore(fileMeta[0].id!!)
        assert(!updated.deleted)
    }

    @Test
    fun `test restore file exception`() {
        assertThrows<NotFoundException> { fileMetaService.restore("id-not-existed") }
    }

    @Test
    fun `test archive`() {
        fileMetaRepository.deleteAll();
        val fileMeta = listOf(FileMeta(
            name = "test1",
            type = FileType.IMAGE,
            createdAt = LocalDate.now(),
            base = "/base",
            caption = "caption",
            src = EncodeUtil.encode("src"),
            thumb = EncodeUtil.encode("thumb"),
            size = 100,
            favorite = true,
            deleted = true,
            archived = false
        )).let { fileMetaRepository.saveAll(it) }

        val updated = fileMetaService.archive(fileMeta[0].id!!)
        assert(updated.archived)
    }

    @Test
    fun `test archive exception`() {
        assertThrows<NotFoundException> { fileMetaService.archive("id-not-existed") }
    }

    @Test
    fun `test delete first time`() {
        fileMetaRepository.deleteAll();
        val fileMeta = listOf(FileMeta(
            name = "test1",
            type = FileType.IMAGE,
            createdAt = LocalDate.now(),
            base = "/base",
            caption = "caption",
            src = EncodeUtil.encode("src"),
            thumb = EncodeUtil.encode("thumb"),
            size = 100,
            favorite = true,
            deleted = false,
            archived = false
        )).let { fileMetaRepository.saveAll(it) }

        fileMetaService.delete(fileMeta[0].id!!)

        assert(fileMetaService.getDeleted(PageRequest.of(0, 100)).content.size == 1)
        assert(fileMetaService.getFileMeta(LocalDate.now(), "", FileType.IMAGE, PageRequest.of(0, 100)).content.size == 0)
    }

    @Test
    fun `test delete permantly`() {
        fileMetaRepository.deleteAll();
        val fileMeta = listOf(FileMeta(
            name = "test1",
            type = FileType.IMAGE,
            createdAt = LocalDate.now(),
            base = "/base",
            caption = "caption",
            src = EncodeUtil.encode("src"),
            thumb = EncodeUtil.encode("thumb"),
            size = 100,
            favorite = true,
            deleted = false,
            archived = false
        )).let { fileMetaRepository.saveAll(it) }

        fileMetaService.delete(fileMeta[0].id!!)
        assert(fileMetaService.getDeleted(PageRequest.of(0, 100)).content.size == 1)

        fileMetaService.delete(fileMeta[0].id!!)
        assert(fileMetaService.getDeleted(PageRequest.of(0, 100)).content.size == 0)
    }

    @Test
    fun `test delete exception`() {
        assertThrows<NotFoundException> { fileMetaService.delete("id-not-existed") }
    }
}
