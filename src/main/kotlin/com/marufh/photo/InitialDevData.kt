package com.marufh.photo

import com.marufh.photo.security.entity.RoleType
import com.marufh.photo.album.entity.Album
import com.marufh.photo.album.repository.AlbumRepository
import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.entity.FileType
import com.marufh.photo.file.repository.FileMetaRepository
import com.marufh.photo.security.entity.Role
import com.marufh.photo.security.entity.User
import com.marufh.photo.security.repository.RoleRepository
import com.marufh.photo.security.repository.UserRepository
import com.marufh.photo.tenant.TenantContext
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import util.EncodeUtil
import java.time.LocalDate
import java.time.LocalDateTime

@Component
@Profile("dev")
class InitialDevData(
    val roleRepository: RoleRepository,
    val userRepository: UserRepository,
    val albumRepository: AlbumRepository,
    val fileMetaRepository: FileMetaRepository,
    val bCryptPasswordEncoder: BCryptPasswordEncoder
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        log.info(".... init application for development env...")

        TenantContext.setCurrentTenant("localhost");

        createRole()
        createUser()
        createAlbum()
        createFileMeta()
    }

    fun createFileMeta() {
        if (fileMetaRepository.findAll().isNotEmpty()) {
            return
        }
        val fileMeta = FileMeta(
            name = "My File",
            caption = "My File Description",
            base = "/base",
            src = EncodeUtil.encode("src"),
            thumb = EncodeUtil.encode("thumb"),
            size = 1000,
            type = FileType.IMAGE,
            createdAt = LocalDate.now(),
        )
        fileMetaRepository.save(fileMeta)
    }

    fun createAlbum() {
        if (albumRepository.findAll().isNotEmpty()) {
            return
        }

        val album = Album(
            name = "My Album",
            description = "My Album Description",
            date = LocalDate.of(2022, 12, 2),
        )
        album.createdAt = LocalDateTime.now()
        album.createdBy = "System"
        album.updatedAt = LocalDateTime.now()
        album.updatedBy = "System"

        albumRepository.save(album)
    }

    private fun createRole() {
        if (roleRepository.findAll().isNotEmpty()) {
            return
        }

        val roles: MutableList<Role> = ArrayList<Role>()
        for (roleType in RoleType.entries) {
            roles.add(Role(name = roleType))
        }
        roleRepository.saveAll(roles)
    }

    private fun createUser() {
        if (userRepository.findAll().isNotEmpty()) {
            return
        }


        val admin = User(
            firstName = "First",
            lastName = "Last",
            email = "admin@gmail.com",
            password = bCryptPasswordEncoder.encode("123456"),
            roles = setOf(roleRepository.findByName(RoleType.ROLE_ADMIN)),
            enabled = true,
        )
        userRepository.save(admin)
    }
}
