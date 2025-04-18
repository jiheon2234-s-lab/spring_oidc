<template>
  <div>
    <h2>Google 로그인 완료</h2>
    <div v-if="loading">토큰을 요청 중입니다...</div>
    <div v-else-if="error">❌ 오류 발생: {{ error }}</div>
    <div v-else-if="tokens">
      <p><strong>✅ Access Token:</strong> {{ tokens.access_token }}</p>
      <p><strong>🪪 ID Token:</strong> {{ tokens.id_token }}</p>
    </div>

    <div v-if="claims">
      <h3>🔍 서버에서 검증된 사용자 정보</h3>
      <p><strong>sub:</strong> {{ claims.sub }}</p>
      <p><strong>email:</strong> {{ claims.email }}</p>
      <p><strong>name:</strong> {{ claims.name }}</p>
      <p><strong>picture:</strong> <img :src="claims.picture" width="80"/></p>
    </div>


    <template>
      <div>
        <h3>만약 조작된 id_token을 보낸다면?</h3>
        <button :disabled="tamperLoading" @click="sendTamperedToken">
          {{ tamperLoading ? '테스트 중…' : '조작된 토큰 전송' }}
        </button>
        <p v-if="tamperError" class="error">❌ {{ tamperError }}</p>
      </div>
    </template>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue'
import {useRoute} from 'vue-router'

const config = useRuntimeConfig()
const route = useRoute()

const loading = ref(true)
const error = ref('')
const tokens = ref<{ access_token: string; id_token: string } | null>(null)
const claims = ref<Record<string, any> | null>(null)

onMounted(async () => {
  const code = route.query.code as string | undefined

  if (!code) {
    error.value = 'code가 없습니다.'
    loading.value = false
    return
  }

  try {
    // 1) Google에 토큰 요청
    const params = new URLSearchParams()
    params.set('code', code)
    params.set('client_id', config.public.googleClientId)
    params.set('client_secret', config.public.googleClientSecret)
    params.set('redirect_uri', 'http://localhost:3000/google-redirect')
    params.set('grant_type', 'authorization_code')

    const response: any = await $fetch('https://oauth2.googleapis.com/token', {
      method: 'POST',
      body: params,
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    })

    tokens.value = {
      access_token: response.access_token,
      id_token: response.id_token,
    }

    // 2) Spring 백엔드에 id_token 보내서 검증 요청
    const jwtClaims = await $fetch('http://localhost:8080/google-id-token', {
      method: 'POST',
      body: response.id_token,
      headers: {'Content-Type': 'application/json'},
    })

    // Spring이 리턴한 Jwt 객체는 { "claims": { ... } } 형태이므로
    claims.value = jwtClaims.claims

  } catch (e: any) {
    console.error(e)
    error.value = e?.data?.error_description || e?.message || '알 수 없는 에러'
  } finally {
    loading.value = false
  }
})


const tamperLoading = ref(false)
const tamperError = ref('')

/**
 * “조작된” JWT를 만들어서 백엔드에 보내보고,
 * 검증 로직이 서명 불일치로 거부하는지 테스트
 */
async function sendTamperedToken() {
  tamperError.value = ''
  tamperLoading.value = true

  // 1) 실제 구글에서 받은 id_token 을 가져오거나,
  //    여기서는 간단히 'invalid.token.value' 처럼 아무 문자열을 써도 됩니다.
  const fakeToken = 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJuYW1lIjoiVGVzdCBVc2VyIiwiaWF0IjoxNzQ0OTQyMTU3LCJleHAiOjQ4OTg1NDIxNTd9.FsMnvwBsjAQ6okb3TUB06MGb6YoyNazsB7agpQci_8qx23VC5ewooW5s6k-UjIw4L1wb_83ya6F3JgqFvv1EBkpiBylHuiEUFCIpsvFvFxM_uGG9D0dFD9_PlRgXNXScifR6ddvlxI0NB4fd_27kX77_Q7GvXT0J36EI_mSxpuuwkocneB7_1HuItnovWNDzT__bw-MrivYDzvu765rkNyheHr3wwvFNHBcatdtCDjvorrzpiP1pzqu6mvUYvr-Qss50Joi-YnElOYFrrsVRh_QfBhiI0Wf4iBDNHnmlMH6CfXXAnRfUrCZLXFJtgfQQBlhfeFsedWRvarZY_tCrqw'

  try {
    await $fetch('http://localhost:8080/google-id-token', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(fakeToken),
    })
    // 만약 여기까지 왔다면… 검증이 잘못 통과했다는 뜻!
    tamperError.value = '⚠️ 검증이 통과되어서는 안 됩니다!'
  } catch (e: any) {
    // 서명 불일치나 알고리즘 오류 등 JwtException이 발생하면 이쪽으로 옵니다.
    tamperError.value = '조작된 토큰 거부됨: ' + (e.message || e?.data?.error || 'Unknown error')
  } finally {
    tamperLoading.value = false
  }
}
</script>
