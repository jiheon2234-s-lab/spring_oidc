package com.example.demo.controller

import com.example.demo.config.Oauth2CustomProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

private val logger = KotlinLogging.logger {}


@Controller
class MainController(
    private val oauth2CustomProperties: Oauth2CustomProperties
) {

    /**
     * index.html렌더링
     */
    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("oauth2CustomProperties", oauth2CustomProperties)
        return "index"
    }

}