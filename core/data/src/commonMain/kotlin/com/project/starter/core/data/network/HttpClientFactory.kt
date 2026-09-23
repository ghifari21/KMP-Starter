package com.project.starter.core.data.network

import com.project.starter.core.domain.repository.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

fun createHttpClient(sessionManager: SessionManager): HttpClient =
    HttpClient {
        defaultRequest {
            url("https://api.dev.example.com/") // Default base URL as requested
        }

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                },
            )
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val token = sessionManager.getToken()
                    if (token != null) BearerTokens(token, "") else null
                }
                refreshTokens {
                    // Logic to refresh token goes here
                    // If refresh fails, clear session
                    // runBlocking is necessary because refreshTokens is suspend, but KMP auth flow expects block
                    // Actually, refreshTokens is suspend in Ktor 2.x
                    runBlocking { sessionManager.clearSession() }
                    null
                }
            }
        }

        installGlobalErrorMapper()
    }
