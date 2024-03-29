package com.marufh.photo

import com.marufh.photo.album.repository.AlbumRepository
import com.marufh.photo.album.service.AlbumService
import com.marufh.photo.config.FilePathProperties
import com.marufh.photo.file.repository.FileMetaRepository
import com.marufh.photo.file.service.*
import com.marufh.photo.security.jwt.TokenProvider
import com.marufh.photo.tenant.TenantContext
import org.junit.jupiter.api.BeforeEach
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
abstract class AbstractServiceTest {

    val log = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var filePathProperties: FilePathProperties

    @Autowired
    lateinit var albumService: AlbumService

    @Autowired
    lateinit var fileMetaService: FileMetaService

    @Autowired
    lateinit var fileUploadService: FileUploadService

    @Autowired
    lateinit var fileDownloadService: FileDownloadService

    @Autowired
    lateinit var fileCountService: FileCountService

    @Autowired
    lateinit var fileCleanService: FileCleanService

    @Autowired
    lateinit var albumRepository: AlbumRepository

    @Autowired
    lateinit var thumbService: ThumbService

    @Autowired
    lateinit var fileMetaRepository: FileMetaRepository

    @Autowired
    lateinit var tokenProvider: TokenProvider

    @BeforeEach
    fun setUp() {
        TenantContext.setCurrentTenant("test");
    }

}
