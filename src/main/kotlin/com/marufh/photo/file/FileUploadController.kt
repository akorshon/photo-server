package com.marufh.photo.file

import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.service.FileUploadService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * This controller is responsible for uploading all types of file
 * This api is used by the client to upload all the files
 * This api is secured and can access with token as header
 *
 * input: MultipartFile
 * output: FileMeta
 */
@RestController
@RequestMapping("/api/private/file")
class FileUploadController(
    val fileUploadService: FileUploadService) {

    @PostMapping
    fun upload(@RequestParam("file") multipartFile: MultipartFile): ResponseEntity<FileMeta> {
        return ResponseEntity.ok<FileMeta>(fileUploadService.upload(multipartFile))
    }
}
