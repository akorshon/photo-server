package com.marufh.photo.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.Errors
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.*


@ControllerAdvice
class ExceptionHandlerAdvice {

    @ExceptionHandler(BadRequestException::class)
    fun badRequest(ex: BadRequestException): ResponseEntity<ExceptionResponse?>? {
        ex.printStackTrace()
        val response = ExceptionResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.reasonPhrase,
            Collections.singletonList(ex.message)
        )
        return ResponseEntity<ExceptionResponse?>(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(NotFoundException::class)
    fun resourceNotFound(ex: NotFoundException): ResponseEntity<ExceptionResponse?>? {
        ex.printStackTrace()
        val response = ExceptionResponse(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.reasonPhrase,
            Collections.singletonList(ex.message)
        )
        return ResponseEntity<ExceptionResponse?>(response, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(AlreadyExistException::class)
    fun alreadyExist(ex: AlreadyExistException): ResponseEntity<ExceptionResponse?>? {
        ex.printStackTrace()
        val response = ExceptionResponse(
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.reasonPhrase,
            Collections.singletonList(ex.message)
        )
        return ResponseEntity<ExceptionResponse?>(response, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun unauthorized(ex: UnauthorizedException): ResponseEntity<ExceptionResponse?>? {
        ex.printStackTrace()
        val response = ExceptionResponse(
            HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.UNAUTHORIZED.reasonPhrase,
            Collections.singletonList(ex.message)
        )
        return ResponseEntity<ExceptionResponse?>(response, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(InternalServerException::class)
    fun internalServer(ex: InternalServerException): ResponseEntity<ExceptionResponse?>? {
        ex.printStackTrace()
        val response = ExceptionResponse(
            HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.UNAUTHORIZED.reasonPhrase,
            Collections.singletonList(ex.message)
        )
        return ResponseEntity<ExceptionResponse?>(response, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun invalidInput(ex: MethodArgumentNotValidException): ResponseEntity<ExceptionResponse?>? {
        ex.printStackTrace()
        val result = ex.bindingResult
        val response = ExceptionResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.reasonPhrase,
            fromBindingErrors(result)
        )
        return ResponseEntity<ExceptionResponse?>(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ValidationException::class)
    fun invalidInput(ex: ValidationException): ResponseEntity<ExceptionResponse?>? {
        ex.printStackTrace()
        val response = ExceptionResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.reasonPhrase,
            Collections.singletonList(ex.message)
        )
        return ResponseEntity<ExceptionResponse?>(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(RuntimeException::class)
    fun runtimeException(ex: RuntimeException): ResponseEntity<ExceptionResponse?>? {
        ex.printStackTrace()
        val response = ExceptionResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            Collections.singletonList("We are sorry! Something wrong on the server.")
        )
        return ResponseEntity<ExceptionResponse?>(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun fromBindingErrors(errors: Errors): List<String> {
        val validErrors: MutableList<String> = ArrayList()
        for (objectError in errors.allErrors) {
            validErrors.add(objectError.defaultMessage!!)
        }
        return validErrors
    }
}
