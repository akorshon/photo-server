package com.marufh.photo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "file.path")
data class FilePathProperties(
    var base: String = "",
    var media: String = "",
    var thumb: String = "",
    var tmp: String = "",
)

