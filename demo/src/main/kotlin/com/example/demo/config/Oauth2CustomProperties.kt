package com.example.demo.config

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}


@Component
@ConfigurationProperties(prefix = "oauth.custom")
@PropertySource(value = ["classpath:application.yml"])
class Oauth2CustomProperties {

    lateinit var googleClientId: String
    lateinit var googleClientSecret: String

    lateinit var naverClientId: String
    lateinit var naverClientSecret: String

    @PostConstruct
    fun validate() {
        // 값이 비어있는지 검증

        logger.info {
            """\n
            googleClientId = $googleClientId, clientSecret = $googleClientSecret
            naverClientId = $naverClientId naverClientSecret= $naverClientSecret
        """.trimMargin()
        }

        if (googleClientId.isBlank() || googleClientSecret.isBlank() || naverClientId.isBlank() || naverClientSecret.isBlank()) {
            throw IllegalStateException()
        }
    }
}