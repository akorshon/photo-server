package com.marufh.photo.security.service

import com.marufh.photo.security.entity.User
import com.marufh.photo.security.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: UserRepository) {

    fun findByEmail(email: String) = userRepository.findByEmail(email)

    fun save(user: User) = userRepository.save(user)

    fun findById(id: String): User? = userRepository.findByIdOrNull(id)
}
