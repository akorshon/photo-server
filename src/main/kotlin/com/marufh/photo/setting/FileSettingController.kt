package com.marufh.photo.setting

import com.marufh.photo.file.service.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/private/setting")
class FileSettingController(
    val fileMetaService: FileMetaService,
    val thumbService: ThumbService,
    val fileVisitService: FileVisitService,
    val fileCleanService: FileCleanService) {

    @GetMapping("/fix-date-by-name")
    fun fixDateByName() {
        fileMetaService.fixCreateDateByName()
    }

    @GetMapping("/generate-hash")
    fun generateHash() {
        fileMetaService.generateHash()
    }

    @GetMapping("/visit")
    fun visitFile() {
        fileVisitService.visitFile()
    }

    @GetMapping("/generate-thumb")
    fun generateThumb() {
        thumbService.generateThumb()
    }

    @GetMapping("/clean-up")
    fun cleanUpFile() {
        fileCleanService.cleanUp()
    }
}
