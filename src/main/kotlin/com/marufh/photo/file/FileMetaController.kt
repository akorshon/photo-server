package com.marufh.photo.file

import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.entity.FileType
import com.marufh.photo.file.service.FileMetaService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/private/file-meta")
class FileMetaController(
    val fileMetaService: FileMetaService) {

    @GetMapping
    fun getFileMeta(
        @RequestParam(required = false, name = "name") name: String?,
        @RequestParam(required = false, name = "date") dateStr: Optional<String>,
        @RequestParam(required = false, name = "type") type: String?,
        pageable: Pageable): Any {

        var localDate: LocalDate? = null
        if (dateStr.isPresent) {
            localDate = LocalDate.parse(dateStr.get())
        }
        return fileMetaService.getFileMeta(localDate, name.orEmpty(), FileType.valueOf(type.orEmpty()), pageable)
    }

    @GetMapping("archived")
    fun getArchivedFileMeta(pageable: Pageable): Page<FileMeta> {
        return fileMetaService.getArchived(pageable)
    }

    @GetMapping("/deleted")
    fun getDeletedFileMeta(pageable: Pageable): Page<FileMeta> {
        return fileMetaService.getDeleted(pageable)
    }

    @GetMapping("/favorite")
    fun getFavoriteFileMeta(pageable: Pageable): Page<FileMeta> {
        return fileMetaService.getFavorite(pageable)
    }

    @GetMapping("/{id}/favorite/{favorite}")
    fun favoritePhoto(@PathVariable id: String, @PathVariable favorite: Boolean): FileMeta {
        return fileMetaService.favorite(id, favorite)
    }

    @GetMapping("/{id}/restore")
    fun restorePhoto(@PathVariable id: String) {
        fileMetaService.restore(id)
    }

    @PutMapping("archived/{id}")
    fun archivedPhoto(@PathVariable id: String): FileMeta {
        return fileMetaService.archive(id)
    }

    @DeleteMapping("/{id}")
    fun deletePhoto(@PathVariable id: String) {
        fileMetaService.delete(id)
    }

}
