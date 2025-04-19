// https://nuxt.com/docs/api/configuration/nuxt-config


export default defineNuxtConfig({
    compatibilityDate: '2024-11-01',
    devtools: {enabled: true},

    runtimeConfig: {
        public: {
            googleClientId: "GOOGLE_CLIENT_ID" as string,
            googleClientSecret: "GOOGLE_CLIENT_SECRET" as string
        }
    }
})
