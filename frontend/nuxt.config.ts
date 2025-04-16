// https://nuxt.com/docs/api/configuration/nuxt-config


export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },

  runtimeConfig: {
    public: {
      googleClientId: process.env.NUXT_PUBLIC_GOOGLE_CLIENT_ID as string,
      redirectUri: process.env.NUXT_PUBLIC_REDIRECT_URI as string
    }
  }
})
