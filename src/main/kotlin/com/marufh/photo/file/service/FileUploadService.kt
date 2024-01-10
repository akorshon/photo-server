package com.marufh.photo.file.service

import com.marufh.photo.config.FilePathProperties
import com.marufh.photo.exception.AlreadyExistException
import com.marufh.photo.exception.FileUploadException
import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.security.jwt.TokenProvider
import com.marufh.photo.tenant.TenantContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import util.EncodeUtil
import util.FileUtil
import java.nio.file.Path

@Service
class FileUploadService(
    val thumbService: ThumbService,
    val filePathProperties: FilePathProperties,
    val fileServiceBase: FileServiceBase,
    val tokenProvider: TokenProvider) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun upload(multipartFile: MultipartFile): FileMeta {
        log.info("Uploading file: {}", multipartFile.originalFilename)

        val fileName = multipartFile.originalFilename!!

        // 1. Validate file name and extension
        if (!FileUtil.validateFileNameAndExt(fileName)) {
            throw FileUploadException("File name or extension is not valid")
        }

        // 2. Save file on tmp location
        val file = fileServiceBase.saveOnTmpDirectory(multipartFile)

        // 3. Get file created date and prepare location
        val dateDirectory: String = FileUtil.createdDate(file)
        val fileDestinationLocation = filePathProperties.media + dateDirectory + "/" + fileName
        val thumbDestinationLocation = filePathProperties.thumb + dateDirectory + "/" + fileName

        // 4. Check the file is existed or not
        if (fileServiceBase.fileExist(fileDestinationLocation)) {
            fileServiceBase.deleteFile(file)
            val src: String = EncodeUtil.encode(
                Path.of(filePathProperties.base).relativize(
                    Path.of(
                        filePathProperties.media
                    )
                ).toString() + "/" + dateDirectory + "/" + fileName
            ) + tmpToken
            throw AlreadyExistException("$fileName is already exist on $dateDirectory <a target='_blank' href='/api/file/$src'>View</a>")
        }

        // 5. Move file to final destination
        val destinationPath = fileServiceBase.moveOnFinalDestination(file, fileDestinationLocation)

        // 6. create thumb
        thumbService.create(destinationPath.toFile(), thumbDestinationLocation)

        // 7. Save file on db
        return fileServiceBase.saveFile(destinationPath.toFile(), dateDirectory)
    }

    private val tmpToken: String
        get() = "?token=" + tokenProvider.createFileToken(10080, TenantContext.getCurrentTenant())
}
