package com.marufh.photo.file

import com.marufh.photo.file.service.FileCountService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/private/file/count")
class FileCountController(
    val fileCountService: FileCountService) {

    @GetMapping
    fun countFile(): ResponseEntity<List<Any>> = ResponseEntity.ok(fileCountService.fileCountByDate())

}
