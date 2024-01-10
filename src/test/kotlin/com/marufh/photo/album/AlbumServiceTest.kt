package com.marufh.photo.album

import com.marufh.photo.AbstractServiceTest
import com.marufh.photo.album.dto.AlbumDto
import com.marufh.photo.album.entity.AlbumAction
import com.marufh.photo.album.repository.AlbumRepository
import com.marufh.photo.album.service.AlbumService
import com.marufh.photo.exception.NotFoundException
import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.entity.FileType
import com.marufh.photo.file.repository.FileMetaRepository
import com.marufh.photo.tenant.TenantContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate


class AlbumServiceTest: AbstractServiceTest() {

    @Test
    @WithMockUser(username = "test@gmail.com", password = "test", roles = ["USER"]) // It's necessary for audit data
    fun `test album creation`() {
        val albumDto = albumService.create(
            AlbumDto(
            name = "test album",
            description = "test album description",
            date = LocalDate.now())
        )

        assert(albumDto.id != null)
        assert(albumDto.name == "test album")
        assert(albumDto.description == "test album description")
    }

    @Test
    @WithMockUser(username = "test@gmail.com", password = "test", roles = ["USER"])
    fun `test album update`() {

        val albumDto = albumService.create(
            AlbumDto(
                name = "test album",
                description = "test album description",
                date = LocalDate.now()
            )
        )

        val updatedAlbumDto = albumService.update(
            AlbumDto(
                id = albumDto.id,
                name = "test album updated",
                description = "test album description updated",
                date = LocalDate.now()
            )
        )

        assert(updatedAlbumDto.id != null)
        assert(updatedAlbumDto.name == "test album updated")
        assert(updatedAlbumDto.description == "test album description updated")
    }

    @Test
    @WithMockUser(username = "test@gmail.com", password = "test", roles = ["USER"])
    fun `test album update not found exception`() {
        assertThrows<NotFoundException> {
            albumService.update(
                AlbumDto(
                    id = "id-not-exist",
                    name = "test album updated",
                    description = "test album description updated",
                    date = LocalDate.now()
                )
            )
        }
    }

    @Test
    @WithMockUser(username = "test@gmail.com", password = "test", roles = ["USER"])
    fun `find album by id`() {
        val albumDto = albumService.create(
            AlbumDto(
                name = "test album 1",
                description = "test album description 1",
                date = LocalDate.now()
            )
        )
        assert(albumService.findById(albumDto.id!!).id == albumDto.id)
    }

    @Test
    @WithMockUser(username = "test@gmail.com", password = "test", roles = ["USER"])
    fun `find album by id exception`() {

        assertThrows<NotFoundException> {
            albumService.findById("id-not-exist")
        }
    }

    @Test
    @WithMockUser(username = "test@gmail.com", password = "test", roles = ["USER"])
    fun `find all albums`(){

        albumRepository.deleteAll()

        albumService.create(
            AlbumDto(
                name = "test album 1",
                description = "test album description 1",
                date = LocalDate.now()
            )
        )

        albumService.create(
            AlbumDto(
                name = "test album 2",
                description = "test album description 2",
                date = LocalDate.now()
            )
        )
        assert(albumService.findAll().size == 2)
    }

    @Test
    @WithMockUser(username = "test@gmail.com", password = "test", roles = ["USER"])
    fun `test album delete`() {

        val albumDto = albumService.create(
            AlbumDto(
                name = "test album 1",
                description = "test album description 1",
                date = LocalDate.now()
            )
        )
        albumService.delete(albumDto.id!!)
        assertThrows<NotFoundException> { albumService.findById(albumDto.id!!) }
    }

    @Test
    @WithMockUser(username = "test@gmail.com", password = "test", roles = ["USER"])
    fun `find files by album id`() {

        val files = fileMetaRepository.saveAll(
            listOf(
                FileMeta(
                    name = "test file1",
                    type = FileType.IMAGE,
                    size = 100,
                    base = "base/path1",
                    src = "test/path1",
                    thumb = "/test/thumbnail1",
                    favorite = false,
                    deleted = false,
                    archived = false,
                    createdAt = LocalDate.now()
                ),
                FileMeta(
                    name = "test file2",
                    type = FileType.IMAGE,
                    size = 100,
                    base = "base/path2",
                    src = "test/path2",
                    thumb = "/test/thumbnail2",
                    favorite = false,
                    deleted = false,
                    archived = false,
                    createdAt = LocalDate.now()
                )
            )
        )


        val albumDto = albumService.create(
            AlbumDto(
                name = "test album 1",
                description = "test album description 1",
                date = LocalDate.now(),
            )
        )

        albumService.manageFile(albumDto.id!!, files[0].id!!, AlbumAction.ADD.name);
        albumService.manageFile(albumDto.id!!, files[1].id!!, AlbumAction.ADD.name);

        val album = albumService.findPhotoByAlbumId(albumDto.id!!)
        assert(album.files.size == 2)
    }

    @Test
    @WithMockUser(username = "test@gmail.com", password = "test", roles = ["USER"])
    fun `find files by album id exception`() {
        assertThrows<NotFoundException> { albumService.findPhotoByAlbumId("id-not-exist") }
    }
}
