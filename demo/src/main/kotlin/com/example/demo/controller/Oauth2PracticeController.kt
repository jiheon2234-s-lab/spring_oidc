package com.example.demo.controller

import com.example.demo.config.Oauth2CustomProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient
import java.util.*

private val logger = KotlinLogging.logger {}

@RestController
class Oauth2PracticeController(
    private val oauth2CustomProperties: Oauth2CustomProperties,
    private val restClient: RestClient,
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
     * @param code Naver가 전달한 authorization code
     *  @return access Token 및 응답 및 id_token(사용자 정보(JSON)를 포함한 Map
     */
    @GetMapping("/custom/oauth2/code/google")
    fun recieveGoogleCodeAnd(@RequestParam code: String): Map<String, Any> {

        logger.info { "code = ${code}" }

        val tokenUrl = "https://oauth2.googleapis.com/token"

        val form = LinkedMultiValueMap<String, String>().apply {
            add("client_id", oauth2CustomProperties.googleClientId)
            add("client_secret", oauth2CustomProperties.googleClientSecret)
            add("code", code)
            add("grant_type", "authorization_code")
            add("redirect_uri", "http://localhost:8080/custom/oauth2/code/google")
        }

        val response = restClient.post()
            .uri("https://oauth2.googleapis.com/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Any>>() {})!!

        val idToken = response.get("id_token") as String
        val idTokenJwt = decodeJwtWithSpring(idToken);  //ID 토큰은 JWT이고, 사용자의 정보를 담고 있다.

        return mapOf("response" to response, "response.id_token" to idTokenJwt)
    }


    /**
     * Naver OAuth2 로그인 이후 콜백을 처리하는 엔드포인트입니다.
     *
     * <p>사용자가 Naver 로그인에 성공하면, Naver가 이 엔드포인트로 authorization code를 전달합니다.</p>
     *
     * <ol>
     *   <li>
     *     사용자가 Naver 로그인 인증 과정을 완료하면,
     *     설정된 redirect_uri인 <code>http://localhost:8080/custom/oauth2/code/naver</code>로
     *     <code>code</code> 파라미터가 포함되어 리디렉션됩니다.
     *   </li>
     *   <li>
     *     서버는 해당 <code>code</code>를 사용해,
     *     <code>https://nid.naver.com/oauth2.0/token</code>으로 POST 요청을 보내 access token을 발급받습니다.
     *   </li>
     *   <li>
     *     access token을 이용해 사용자 정보를 조회하기 위해,
     *     <code>https://openapi.naver.com/v1/nid/me</code>에 GET 요청을 보냅니다.
     *   </li>
     *   <li>
     *     최종적으로 access token과 사용자 정보(JSON)를 포함한 Map을 반환합니다.
     *   </li>
     * </ol>
     *
     * @param code Naver가 전달한 authorization code
     * @return access token 응답 및 사용자 정보(JSON)를 포함한 Map
     */
    @GetMapping("/custom/oauth2/code/naver")
    fun recieveNaverCodeAnd(@RequestParam code: String): Map<String, Any> {

        logger.info { "code = $code" }

        val tokenUri = "https://nid.naver.com/oauth2.0/token"

        val form = LinkedMultiValueMap<String, String>().apply {
            add("client_id", oauth2CustomProperties.naverClientId)
            add("client_secret", oauth2CustomProperties.naverClientSecret)
            add("code", code)
            add("grant_type", "authorization_code")
            add("redirect_uri", "http://localhost:8080/custom/oauth2/code/naver")
        }

        val accessTokenResponse = restClient.post()
            .uri(tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Any>>() {})!!

        logger.info { accessTokenResponse } //id토큰이 없다

        val accessToken = accessTokenResponse.get("access_token") as String

        val userInfoUri = "https://openapi.naver.com/v1/nid/me"

        val userInfo = restClient.get()
            .uri(userInfoUri)
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Any>>() {})!!

        logger.info("body: $userInfo");

        return mapOf(
            "give_token_and_get_accessToken" to accessTokenResponse,
            "final_get_userInfo" to userInfo,
        )
    }


    fun decodeJwtWithSpring(jwtToken: String): Jwt {
        val decoder = object : JwtDecoder {
            override fun decode(token: String): Jwt {
                val parts = token.split(".")
                if (parts.size != 3) throw JwtException("Invalid JWT")

                val headerJson = String(Base64.getUrlDecoder().decode(parts[0]))
                val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))

                val headers = jacksonObjectMapper().readValue(headerJson, Map::class.java) as Map<String, Any>
                val claims = jacksonObjectMapper().readValue(payloadJson, Map::class.java) as Map<String, Any>

                return Jwt(token, null, null, headers, claims)
            }
        }

        val jwt = decoder.decode(jwtToken)
        return jwt
    }
}