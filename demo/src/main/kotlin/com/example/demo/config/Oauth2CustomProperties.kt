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

    @PostConstruct
    fun validate() {
        // isInitialized 체크를 통해 초기화 여부 검증
        if (!::googleClientId.isInitialized || !::googleClientSecret.isInitialized) {
            throw IllegalStateException("OAuth2 properties not initialized: clientId or clientSecret")
        }

        // 값이 비어있는지 검증
        if (googleClientId.isBlank() || googleClientSecret.isBlank()) {
            throw IllegalStateException("Missing Google OAuth configuration: clientId or clientSecret is blank")
        }

        logger.info { "clientId = $googleClientId, clientSecret = $googleClientSecret" }
    }
}