package com.marufh.photo.exception

class ExceptionResponse(
    val errorCode: Int,
    val message: String,
    errors: List<String>
)
