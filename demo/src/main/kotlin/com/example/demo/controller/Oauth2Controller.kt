package com.example.demo.controller

import com.example.demo.config.Oauth2CustomProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient

private val logger = KotlinLogging.logger {}

@RestController
class Oauth2Controller(
    private val oauth2CustomProperties: Oauth2CustomProperties,
    private val restClient: RestClient
) {

    /**
     * Google OAuth2 로그인 후 콜백을 처리하는 엔드포인트입니다.
     *
     * <p>사용자가 구글 로그인에 성공하면, Google이 이 엔드포인트로 authorization code를 전달합니다.</p>
     *
     * <ol>
     *   <li>
     *     사용자가 <code>index.html</code>의 구글 로그인 링크를 클릭하면, 다음과 같은 인증 URL로 이동합니다:<br>
     *     <code>https://accounts.google.com/o/oauth2/v2/auth?client_id=...&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fcustom%2Foauth2%2Fcode%2Fgoogle&response_type=code&scope=email&state=...</code>
     *   </li>
     *   <li>
     *     사용자가 구글 로그인을 성공하면, Google은 <code>http://localhost:8080/custom/oauth2/code/google</code>로
     *     <code>code</code> 파라미터를 포함해 리디렉션합니다.
     *     (이 redirect_uri는 Google Cloud Console에 등록되어 있어야 합니다)
     *   </li>
     *   <li>
     *     서버는 받은 <code>code</code>를 가지고
     *     <code>https://oauth2.googleapis.com/token</code>에 POST 요청을 보내 access token을 요청합니다.
     *   </li>
     *   <li>
     *     요청이 성공하면, Google은 <code>access_token</code>과 OIDC를 사용하는 경우 <code>id_token</code>을 포함한 응답을 반환합니다.
     *   </li>
     * </ol>
     */
    @GetMapping("/custom/oauth2/code/google")
    fun recieveGoogleCode(@RequestParam code: String): Map<String, Any> {

        val tokenUrl = "https://oauth2.googleapis.com/token"

        val form = LinkedMultiValueMap<String, String>().apply {
            add("client_id", oauth2CustomProperties.clientId)
            add("client_secret", oauth2CustomProperties.clientSecret)
            add("code", code)
            add("grant_type", "authorization_code")
            add("redirect_uri", "http://localhost:8080/custom/oauth2/code/google")
        }

        val response: Map<String, Any> = RestClient.builder()
            .baseUrl("https://oauth2.googleapis.com")
            .build()
            .post()
            .uri("/token")
            .headers { it.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE) }
            .body(form)
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Any>>() {})!!

        return response
    }
}