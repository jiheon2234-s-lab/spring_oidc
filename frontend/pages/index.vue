<template>
  <div class="home">
    <h1>환영합니다 👋</h1>
    <button @click="loginWithGoogle">Google로 로그인</button>
  </div>
</template>

<script lang="ts" setup>
const config = useRuntimeConfig()

const loginWithGoogle = () => {
  const clientId = config.public.googleClientId
  const redirectUri = "http://localhost:3000/google-redirect"
  const scope = 'openid email profile'
  const state = Math.random().toString(36).substring(2) // CSRF 방지용

  const googleParams = new URLSearchParams({
    client_id: clientId,
    redirect_uri: redirectUri,
    response_type: "code",
    scope: scope,
    state: state,
    // access_type: "offline",
    // include_granted_scopes: "true"
  });
  const url = `https://accounts.google.com/o/oauth2/v2/auth?${googleParams.toString()}`

  window.location.href = url.toString()
}
</script>
