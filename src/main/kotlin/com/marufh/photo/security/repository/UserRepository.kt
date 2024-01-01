package com.marufh.photo.security.repository

import com.marufh.photo.security.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, String>{

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = ?1")
    fun findByEmail(email: String): User?

    //@Query("SELECT u FROM User u WHERE u.email=:email")
    //fun findByEmail(@Param("email") email: String?): Optional<User?>?
}
