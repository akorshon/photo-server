package com.marufh.photo.file.repository

import com.marufh.photo.file.dto.FileCounter
import com.marufh.photo.file.entity.FileMeta
import com.marufh.photo.file.entity.FileType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface FileMetaRepository : JpaRepository<FileMeta, String> {
    @Query(
        "SELECT f FROM FileMeta f " +
                "WHERE f.deleted=false AND f.archived=false AND " +
                "(?1 IS NULL  OR f.createdAt = ?1 ) AND " +
                "(?2 IS NULL  OR f.name LIKE %?2% )  AND " +
                "(?3 IS NULL  OR f.type = ?3) " +
                "ORDER BY f.name ASC "
    )
    fun getFileByDateAndName(date: LocalDate, name: String, type: FileType, pageable: Pageable): Page<FileMeta>

    @Query("select f from FileMeta f where f.deleted=false and f.createdAt <= ?1 ")
    fun findAll(localDate: LocalDate?, pageable: Pageable?): Page<FileMeta>

    @Query(value = "select f from FileMeta f where f.deleted=true")
    fun findDeleted(pageable: Pageable?): Page<FileMeta>

    @Query(value = "select f from FileMeta f where f.archived=true")
    fun findArchived(pageable: Pageable): Page<FileMeta>

    @Query(value = "select f from FileMeta f where f.favorite=true and f.deleted=false")
    fun findFavorite(pageable: Pageable?): Page<FileMeta>

    @Query(
        value = "SELECT YEAR(created_at) AS year, MONTH(created_at) AS month, DAY(created_at) as day, COUNT(id) AS count FROM FILE_META " +
                "where deleted=false and archived=false and tenant=?1  " +
                "group by year, month, day order by year desc, month desc, day desc ", nativeQuery = true
    )
    fun getFileCount(tenant: String?): List<FileCounter?>? /*@Query(value = "" +
            "SELECT  f.* " +
            "    FROM FILE_META f " +
            "    INNER JOIN ( " +
            "            SELECT name " +
            "  FROM FILE_META" +
            "          GROUP BY size " +
            "          HAVING count(name) > 1 " +
            "            ) dupes ON f.name = dupes.name where f.deleted=false" +
            "",
            countQuery = "" +
                    "SELECT  count(*) " +
                    "    FROM FILE_META f " +
                    "    INNER JOIN ( " +
                    "            SELECT name  " +
                    "  FROM FILE_META  " +
                    "          GROUP BY name " +
                    "          HAVING count(name) > 1 " +
                    "            ) dupes ON f.name = dupes.name where f.deleted=false" +
                    "",
            nativeQuery = true)
    Page<FileMeta> findDuplicateByName(Pageable pageable);
    */
    /*@Query(value = "" +
            "SELECT  f.* " +
            "    FROM FILE_META f " +
            "    INNER JOIN ( " +
            "            SELECT name " +
            "  FROM FILE_META" +
            "          GROUP BY name " +
            "          HAVING count(*) > 1 " +
            "            ) dupes ON f.name = dupes.name where f.deleted=false ORDER BY f.name " +
            "",
            countQuery = "" +
                    "SELECT  count(*) " +
                    "    FROM FILE_META f " +
                    "    INNER JOIN ( " +
                    "            SELECT name  " +
                    "  FROM FILE_META  " +
                    "          GROUP BY name " +
                    "          HAVING count(*) > 1 " +
                    "            ) dupes ON f.name = dupes.name where f.deleted=false" +
                    "",
            nativeQuery = true)
    Page<FileMeta> findDuplicateByHash(Pageable pageable);
    */
}
