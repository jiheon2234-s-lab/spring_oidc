package com.example.demo.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


private val logger = KotlinLogging.logger {}

@RestController
class Oauth2WithFrontendController(
    private val decoder: JwtDecoder = JwtDecoders.fromIssuerLocation("https://accounts.google.com")
) {

    @PostMapping("google-id-token")
    fun getIdTokenFromFrontend(@RequestBody idToken: String): Jwt {
        val jwt = decoder.decode(idToken)
        logger.info { jwt }
        return jwt
    }


}