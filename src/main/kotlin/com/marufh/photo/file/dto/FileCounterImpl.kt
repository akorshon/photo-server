package com.marufh.photo.file.dto


class FileCounterImpl(
    override val year: Int?,
    override val month: Int?,
    override val day: Int?,
    override val count: Int?,
    override val page: Int?) : FileCounter
