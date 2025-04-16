package com.example.demo.filter

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper

private val logger = KotlinLogging.logger {}

@Component
class RequestLoggingFilter(
    private val objectMapper: ObjectMapper = ObjectMapper()
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val wrappedRequest = ContentCachingRequestWrapper(request)
        logRequest(wrappedRequest)
        filterChain.doFilter(wrappedRequest, response)
    }

    private fun logRequest(request: ContentCachingRequestWrapper) {
        val logMap = mutableMapOf<String, Any?>()

        logMap["method"] = request.method
        logMap["uri"] = request.requestURI
        logMap["queryString"] = request.queryString

        val headers = mutableMapOf<String, String>()
        request.headerNames?.asIterator()?.forEachRemaining {
            headers[it] = request.getHeader(it)
        }
        logMap["headers"] = headers

        val params = request.parameterMap.mapValues { it.value.toList() }
        logMap["parameters"] = params

        val body = request.contentAsByteArray.takeIf { it.isNotEmpty() }?.let {
            String(it, Charsets.UTF_8)
        } ?: ""
        logMap["body"] = body

        val json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(logMap)
        logger.info("[HTTP Request]\n$json")
    }
}