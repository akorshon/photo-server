package com.marufh.photo.file.service

import com.marufh.photo.file.dto.FileCounter
import com.marufh.photo.file.dto.FileCounterImpl
import com.marufh.photo.file.repository.FileMetaRepository
import com.marufh.photo.tenant.TenantContext
import org.springframework.stereotype.Service
import kotlin.math.min

@Service
class FileCountService(
    val fileMetaRepository: FileMetaRepository) {

    companion object {
        private const val MAX_FILE_IN_A_PAGE = 100
        private const val INITIAL_PAGE = 0
    }

    fun fileCountByDate(): List<FileCounter> {
            val counterList: List<FileCounter> = fileMetaRepository.getFileCount(TenantContext.getCurrentTenant())
            val result: MutableList<FileCounter> = ArrayList()
            for (counter in counterList) {
                if (counter.count!! > MAX_FILE_IN_A_PAGE) {
                    var page = INITIAL_PAGE
                    var count: Int = counter.count!!
                    while (count > 0) {
                        result.add(
                            createFileCounter(
                                counter, min(count.toDouble(), MAX_FILE_IN_A_PAGE.toDouble())
                                    .toInt(), page++
                            )
                        )
                        count -= MAX_FILE_IN_A_PAGE
                    }
                } else {
                    result.add(createFileCounter(counter, counter.count!!, INITIAL_PAGE))
                }
            }
            return result
        }

    private fun createFileCounter(counter: FileCounter, count: Int, page: Int): FileCounterImpl {
        return FileCounterImpl(
            year = counter.year,
            month = counter.month,
            day = counter.day,
            count = count,
            page = page,
        )
    }
}
