package com.marufh.photo.file

import com.marufh.photo.file.service.FileDownloadService
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * This is public api to download file.
 * Files are secured and can access with token as request param
 * This api is used by the client to download all the files
 *
 * input: file name, token
 * output: file as resource
 */
@RestController
@RequestMapping("/api/file")
class FileDownloadController(
    val fileDownloadService: FileDownloadService) {

    @GetMapping("/{file}")
    fun downloadFile(@PathVariable file: String, @RequestParam token: String): ResponseEntity<Resource> {
        return fileDownloadService.getFileAsResource(file, token)
    }
}
