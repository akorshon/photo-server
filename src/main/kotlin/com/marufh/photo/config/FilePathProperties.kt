package com.marufh.photo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "file.path")
data class FilePathProperties(
    val base: String = "",
    val media: String = "",
    val thumb: String = "",
    val tmp: String = "",
)
