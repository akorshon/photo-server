package com.marufh.photo.album.repository

import com.marufh.photo.album.entity.Album
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface AlbumRepository : JpaRepository<Album, String> {

    @Query("SELECT a FROM Album a ORDER BY a.date DESC")
    fun findAllOrderByDate(): List<Album?>?

    @Query("SELECT a FROM Album a LEFT JOIN FETCH a.files WHERE a.id = ?1")
    fun findByIdWithFiles(id: String): Optional<Album>

    @Query(
        "SELECT a FROM Album a " +
                "LEFT JOIN a.files file " +
                "WHERE file.id = ?1"
    )
    fun fineByFileId(fileId: String?): List<Album?>?
}
