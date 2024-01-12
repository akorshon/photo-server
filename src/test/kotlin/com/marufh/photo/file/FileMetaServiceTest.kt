package com.marufh.photo.file

import com.marufh.photo.AbstractServiceTest
import com.marufh.photo.exception.NotFoundException
import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.entity.FileType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageRequest
import util.EncodeUtil
import java.time.LocalDate

class FileMetaServiceTest: AbstractServiceTest() {

    @Test
    fun `get file with different filters`() {
        albumRepository.deleteAll()
        fileMetaRepository.deleteAll()
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
    fun `create favorite file`() {
        albumRepository.deleteAll()
        fileMetaRepository.deleteAll()
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
    fun `create favorite file exception`() {
        assertThrows<NotFoundException> { fileMetaService.favorite("id-not-existed", true) }
    }

    @Test
    fun `get favourite files`() {
        albumRepository.deleteAll()
        fileMetaRepository.deleteAll()
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
    fun `create archived file`() {
        albumRepository.deleteAll()
        fileMetaRepository.deleteAll()
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
    fun `create archived file exception`() {
        assertThrows<NotFoundException> { fileMetaService.archive("id-not-existed") }
    }

    @Test
    fun `get archived files`() {
        albumRepository.deleteAll()
        fileMetaRepository.deleteAll()
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
    fun `file soft delete`() {
        albumRepository.deleteAll()
        fileMetaRepository.deleteAll()
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
    fun `file permanently delete`() {
        albumRepository.deleteAll()
        fileMetaRepository.deleteAll()
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
    fun `get deleted files`() {
        albumRepository.deleteAll()
        fileMetaRepository.deleteAll()
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
    fun `file delete exception`() {
        assertThrows<NotFoundException> { fileMetaService.delete("id-not-existed") }
    }

    @Test
    fun `file restore from delete`() {
        albumRepository.deleteAll()
        fileMetaRepository.deleteAll()
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
    fun `file restore from delete exception`() {
        assertThrows<NotFoundException> { fileMetaService.restore("id-not-existed") }
    }

}
